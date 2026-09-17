package com.skittlq.endernium.progression;

import com.skittlq.endernium.item.EnderniumItems;
import com.skittlq.endernium.network.EnderniumNetworking;
import com.skittlq.endernium.network.payloads.BlessingVfxPayload;
import com.skittlq.endernium.particles.EnderniumParticles;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Tracks participation in every real Ender Dragon fight and delivers the visual blessing. */
public final class DragonBlessingTracker {
    private static final int PASSIVE_PARTICIPATION_TICKS = 20 * 30;
    private static final double ARENA_RADIUS_SQR = 192.0 * 192.0;
    private static final int EMERGENCE_END_TICK = DragonBlessingVfxMath.EMERGENCE_END_TICK;
    private static final int GATHERING_END_TICK = DragonBlessingVfxMath.GATHERING_END_TICK;
    private static final int SEEKING_END_TICK = DragonBlessingVfxMath.SEEKING_END_TICK;
    private static final int LINGER_END_TICK = DragonBlessingVfxMath.LINGER_END_TICK;
    private static final int BLESSING_TICKS = DragonBlessingVfxMath.BLESSING_END_TICK;
    private static final int MAX_BLESSING_PARTICLES_PER_TICK = 96;
    private static final int PARTICLES_PER_BLESSING_BUDGET = 16;
    private static final double TRAIL_SAMPLE_SPACING = 0.12;
    private static final Vec3 PORTAL_ORIGIN = new Vec3(0.5, 65.5, 0.5);

    private static MinecraftServer activeServer;
    private static FightSession activeFight;
    private static UUID completedDragon;
    private static final List<BlessingSession> BLESSINGS = new ArrayList<>();
    private static final Set<UUID> ACTIVE_RECIPIENTS = new HashSet<>();

    private DragonBlessingTracker() {
    }

    public static void recordDragonDamage(ServerPlayer attacker, EnderDragon dragon) {
        if (!Level.END.equals(attacker.level().dimension()) || attacker.isSpectator()) {
            return;
        }
        var dragonFight = attacker.level().getDragonFight();
        if (dragonFight == null || !dragon.getUUID().equals(dragonFight.dragonUUID())) {
            return;
        }
        MinecraftServer server = attacker.level().getServer();
        resetForServer(server);
        if (activeFight == null || !activeFight.dragonId.equals(dragon.getUUID())) {
            activeFight = FightSession.start(dragon);
        }
        activeFight.qualifyingPlayers.add(attacker.getUUID());
    }

    public static void tick(MinecraftServer server) {
        resetForServer(server);
        ServerLevel end = server.getLevel(Level.END);
        if (end == null) {
            return;
        }

        tickBlessings(end);
        tickFight(end);
        tickPendingPlayers(server, end);
    }

    public static void clear() {
        activeServer = null;
        activeFight = null;
        completedDragon = null;
        BLESSINGS.clear();
        ACTIVE_RECIPIENTS.clear();
    }

    /** Replays only the blessing visuals and sound without changing progression state. */
    public static boolean startBlessingSimulation(ServerPlayer player, Vec3 origin) {
        if (!Level.END.equals(player.level().dimension()) || player.isSpectator()) {
            return false;
        }
        resetForServer(player.level().getServer());
        BLESSINGS.removeIf(blessing -> blessing.playerId.equals(player.getUUID()));
        ACTIVE_RECIPIENTS.remove(player.getUUID());
        beginBlessing(player, origin, 0, 1, false);
        return true;
    }

    private static void resetForServer(MinecraftServer server) {
        if (activeServer == server) {
            return;
        }
        clear();
        activeServer = server;
    }

    private static void tickFight(ServerLevel end) {
        var dragonFight = end.getDragonFight();
        UUID authoritativeDragon = dragonFight == null ? null : dragonFight.dragonUUID();
        if (authoritativeDragon == null) {
            if (activeFight != null) {
                if (activeFight.deathStarted) {
                    finishFight(end, activeFight.lastDragonPosition);
                } else {
                    activeFight = null;
                }
            }
            return;
        }
        List<? extends EnderDragon> dragons = end.getEntities(EntityTypes.ENDER_DRAGON, dragon -> !dragon.isRemoved());
        EnderDragon tracked = null;
        if (activeFight != null) {
            for (EnderDragon dragon : dragons) {
                if (dragon.getUUID().equals(activeFight.dragonId)) {
                    tracked = dragon;
                    break;
                }
            }
        }

        if (activeFight == null) {
            for (EnderDragon dragon : dragons) {
                if (dragon.getUUID().equals(authoritativeDragon)
                        && !dragon.getUUID().equals(completedDragon)) {
                    activeFight = FightSession.start(dragon);
                    tracked = dragon;
                    break;
                }
            }
        }

        if (activeFight == null) {
            return;
        }

        if (tracked == null) {
            if (activeFight.deathStarted) {
                finishFight(end, activeFight.lastDragonPosition);
            } else {
                activeFight = null;
            }
            return;
        }

        activeFight.lastDragonPosition = tracked.position().add(0.0, tracked.getBbHeight() * 0.55, 0.0);
        activeFight.deathStarted |= tracked.dragonDeathTime > 0;
        tickPassiveParticipation(end, activeFight);
        if (tracked.dragonDeathTime >= 199) {
            finishFight(end, activeFight.lastDragonPosition);
        }
    }

    private static void tickPassiveParticipation(ServerLevel end, FightSession fight) {
        for (ServerPlayer player : end.players()) {
            if (player.isSpectator() || !player.isAlive()) {
                continue;
            }
            double distanceSqr = player.getX() * player.getX() + player.getZ() * player.getZ();
            if (distanceSqr > ARENA_RADIUS_SQR) {
                continue;
            }
            int ticks = fight.nearbyTicks.merge(player.getUUID(), 1, Integer::sum);
            if (ticks >= PASSIVE_PARTICIPATION_TICKS) {
                fight.qualifyingPlayers.add(player.getUUID());
            }
        }
    }

    private static void finishFight(ServerLevel end, Vec3 dragonOrigin) {
        FightSession finished = activeFight;
        activeFight = null;
        completedDragon = finished.dragonId;
        if (finished.qualifyingPlayers.isEmpty()) {
            return;
        }

        EnderniumBlessingSavedData pending = EnderniumBlessingSavedData.get(end.getServer());
        pending.addAll(finished.qualifyingPlayers);
        List<ServerPlayer> immediateRecipients = new ArrayList<>();
        for (UUID playerId : finished.qualifyingPlayers) {
            ServerPlayer player = end.getServer().getPlayerList().getPlayer(playerId);
            if (player != null
                    && player.level() == end
                    && player.isAlive()
                    && !player.isSpectator()
                    && !EnderniumBlessing.isBlessed(player)) {
                immediateRecipients.add(player);
            }
        }
        immediateRecipients.sort((left, right) -> left.getUUID().compareTo(right.getUUID()));
        for (int index = 0; index < immediateRecipients.size(); index++) {
            beginBlessing(immediateRecipients.get(index), dragonOrigin, index, immediateRecipients.size());
        }
    }

    private static void tickPendingPlayers(MinecraftServer server, ServerLevel end) {
        EnderniumBlessingSavedData pending = EnderniumBlessingSavedData.get(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID playerId = player.getUUID();
            if (!pending.contains(playerId) || ACTIVE_RECIPIENTS.contains(playerId)) {
                continue;
            }
            if (EnderniumBlessing.isBlessed(player)) {
                pending.remove(playerId);
                continue;
            }
            if (player.level() == end && player.isAlive() && !player.isSpectator() && hasEnderniumGear(player)) {
                beginBlessing(player, PORTAL_ORIGIN, 0, 1);
            }
        }
    }

    private static void beginBlessing(ServerPlayer player, Vec3 origin, int recipientIndex, int recipientCount) {
        beginBlessing(player, origin, recipientIndex, recipientCount, true);
    }

    private static void beginBlessing(
            ServerPlayer player,
            Vec3 origin,
            int recipientIndex,
            int recipientCount,
            boolean grantsBlessing
    ) {
        if (ACTIVE_RECIPIENTS.add(player.getUUID())) {
            long seed = player.getRandom().nextLong();
            BLESSINGS.add(new BlessingSession(
                    player.getUUID(),
                    origin,
                    seed,
                    recipientIndex,
                    Math.max(1, recipientCount),
                    grantsBlessing
            ));
            BlessingVfxPayload payload = new BlessingVfxPayload(
                    player.getUUID(),
                    player.getId(),
                    origin,
                    seed,
                    recipientIndex,
                    Math.max(1, recipientCount)
            );
            for (ServerPlayer observer : player.level().players()) {
                EnderniumNetworking.sendBlessingVfx(observer, payload);
            }
        }
    }

    private static void tickBlessings(ServerLevel end) {
        int sessionCount = Math.max(1, BLESSINGS.size());
        int totalBudget = Math.min(
                MAX_BLESSING_PARTICLES_PER_TICK,
                sessionCount * PARTICLES_PER_BLESSING_BUDGET
        );
        int baseBudget = totalBudget / sessionCount;
        int remainder = totalBudget % sessionCount;
        int sessionIndex = 0;
        Iterator<BlessingSession> iterator = BLESSINGS.iterator();
        while (iterator.hasNext()) {
            BlessingSession blessing = iterator.next();
            ServerPlayer player = end.getServer().getPlayerList().getPlayer(blessing.playerId);
            if (player == null || player.level() != end || !player.isAlive() || player.isSpectator()) {
                ACTIVE_RECIPIENTS.remove(blessing.playerId);
                iterator.remove();
                continue;
            }

            int rotatedIndex = (sessionIndex + blessing.age) % sessionCount;
            int particleBudget = baseBudget + (rotatedIndex < remainder ? 1 : 0);
            blessing.tick(end, player, particleBudget);
            sessionIndex++;
            if (blessing.age < BLESSING_TICKS) {
                continue;
            }

            if (blessing.grantsBlessing) {
                EnderniumBlessingSavedData.get(end.getServer()).remove(blessing.playerId);
                EnderniumBlessing.bless(player, true);
            }
            ACTIVE_RECIPIENTS.remove(blessing.playerId);
            iterator.remove();
        }
    }

    private static boolean hasEnderniumGear(ServerPlayer player) {
        for (int index = 0; index < player.getInventory().getContainerSize(); index++) {
            if (isEnderniumGear(player.getInventory().getItem(index))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEnderniumGear(ItemStack stack) {
        return stack.is(EnderniumItems.ENDERNIUM_SWORD.get())
                || stack.is(EnderniumItems.ENDERNIUM_SPEAR.get())
                || stack.is(EnderniumItems.ENDERNIUM_PICKAXE.get())
                || stack.is(EnderniumItems.ENDERNIUM_AXE.get())
                || stack.is(EnderniumItems.ENDERNIUM_SHOVEL.get())
                || stack.is(EnderniumItems.ENDERNIUM_HOE.get())
                || stack.is(EnderniumItems.ENDERNIUM_HELMET.get())
                || stack.is(EnderniumItems.ENDERNIUM_CHESTPLATE.get())
                || stack.is(EnderniumItems.ENDERNIUM_LEGGINGS.get())
                || stack.is(EnderniumItems.ENDERNIUM_BOOTS.get());
    }

    private static final class FightSession {
        private final UUID dragonId;
        private final Map<UUID, Integer> nearbyTicks = new HashMap<>();
        private final Set<UUID> qualifyingPlayers = new HashSet<>();
        private Vec3 lastDragonPosition;
        private boolean deathStarted;

        private FightSession(UUID dragonId, Vec3 lastDragonPosition, boolean deathStarted) {
            this.dragonId = dragonId;
            this.lastDragonPosition = lastDragonPosition;
            this.deathStarted = deathStarted;
        }

        private static FightSession start(EnderDragon dragon) {
            return new FightSession(
                    dragon.getUUID(),
                    dragon.position().add(0.0, dragon.getBbHeight() * 0.55, 0.0),
                    dragon.dragonDeathTime > 0
            );
        }
    }

    private static final class BlessingSession {
        private final UUID playerId;
        private final Vec3 origin;
        private final long seed;
        private final double phase;
        private final int recipientIndex;
        private final int recipientCount;
        private final boolean grantsBlessing;
        private float smoothedFacingYaw = Float.NaN;
        private Vec3 previousCore;
        private int age;

        private BlessingSession(
                UUID playerId,
                Vec3 origin,
                long seed,
                int recipientIndex,
                int recipientCount,
                boolean grantsBlessing
        ) {
            this.playerId = playerId;
            this.origin = origin;
            this.seed = seed;
            this.phase = DragonBlessingVfxMath.phase(seed);
            this.recipientIndex = recipientIndex;
            this.recipientCount = recipientCount;
            this.grantsBlessing = grantsBlessing;
        }

        private void tick(ServerLevel level, ServerPlayer player, int particleBudget) {
            Vec3 chestTarget = player.position().add(0.0, player.getBbHeight() * 0.62, 0.0);
            if (Float.isNaN(smoothedFacingYaw)) {
                smoothedFacingYaw = player.getYRot();
            } else {
                smoothedFacingYaw = DragonBlessingVfxMath.followFacingYaw(
                        smoothedFacingYaw,
                        player.getYRot()
                );
            }
            Vec3 frontTarget = DragonBlessingVfxMath.frontTarget(chestTarget, smoothedFacingYaw);
            Vec3 core = DragonBlessingVfxMath.corePosition(
                    origin,
                    chestTarget,
                    frontTarget,
                    seed,
                    recipientIndex,
                    recipientCount,
                    age
            );
            tickTrailParticles(level, core, chestTarget, particleBudget);
            previousCore = core;

            if (age == BLESSING_TICKS - 1) {
                tickBlessingPulse(level, chestTarget, particleBudget);
                level.playSound(
                        null,
                        chestTarget.x, chestTarget.y, chestTarget.z,
                        SoundEvents.ENDER_EYE_DEATH,
                        SoundSource.PLAYERS,
                        0.72F,
                        1.42F
                );
                level.playSound(
                        null,
                        chestTarget.x, chestTarget.y, chestTarget.z,
                        SoundEvents.END_PORTAL_FRAME_FILL,
                        SoundSource.PLAYERS,
                        0.38F,
                        1.72F
                );
            }
            age++;
        }

        private void tickTrailParticles(
                ServerLevel level,
                Vec3 core,
                Vec3 chestTarget,
                int particleBudget
        ) {
            if (particleBudget <= 0) {
                return;
            }

            Vec3 segmentStart = previousCore == null ? core : previousCore;
            double distance = segmentStart.distanceTo(core);
            int count = previousCore == null
                    ? Math.min(4, particleBudget)
                    : Math.min(
                            particleBudget,
                            Math.max(1, Mth.ceil(distance / TRAIL_SAMPLE_SPACING))
                    );
            double spread = trailSpread(core, chestTarget);
            for (int stream = 0; stream < count; stream++) {
                double segmentProgress = previousCore == null
                        ? 1.0
                        : (stream + 1.0) / count;
                Vec3 trailCenter = segmentStart.lerp(core, segmentProgress);
                double angle = phase + stream * 2.39996 + age * 0.46;
                double radialScale = spread * (0.45 + noise(stream, 0) * 0.55);
                Vec3 point = trailCenter.add(
                        Math.cos(angle) * radialScale,
                        Math.sin(angle * 1.37) * radialScale * 0.72,
                        Math.sin(angle) * radialScale
                );
                spawnBit(level, point, Vec3.ZERO);
            }
        }

        private double trailSpread(Vec3 core, Vec3 chestTarget) {
            if (age < EMERGENCE_END_TICK) {
                double progress = age / (double)(EMERGENCE_END_TICK - 1);
                return 0.25 - smoothStep(progress) * 0.08;
            }
            if (age < GATHERING_END_TICK) {
                double progress = (age - EMERGENCE_END_TICK)
                        / (double)(GATHERING_END_TICK - EMERGENCE_END_TICK - 1);
                return 0.17 - smoothStep(progress) * 0.04;
            }
            if (age < SEEKING_END_TICK) {
                double progress = (age - GATHERING_END_TICK)
                        / (double)(SEEKING_END_TICK - GATHERING_END_TICK - 1);
                return 0.11 - Math.sin(progress * Math.PI) * 0.025;
            }
            if (age < LINGER_END_TICK) {
                return 0.13;
            }
            double distanceFactor = Mth.clamp(
                    core.distanceTo(chestTarget) / DragonBlessingVfxMath.ORBIT_RADIUS,
                    0.0,
                    1.0
            );
            return 0.045 + distanceFactor * 0.055;
        }

        private void tickBlessingPulse(ServerLevel level, Vec3 target, int particleBudget) {
            int count = particleBudget;
            for (int stream = 0; stream < count; stream++) {
                double y = 1.0 - 2.0 * (stream + 0.5) / count;
                double horizontal = Math.sqrt(Math.max(0.0, 1.0 - y * y));
                double angle = phase + stream * 2.39996;
                Vec3 direction = new Vec3(
                        Math.cos(angle) * horizontal,
                        y,
                        Math.sin(angle) * horizontal
                );
                spawnBit(level, target.add(direction.scale(0.12)), direction.scale(0.115));
            }
        }

        private void spawnBit(ServerLevel level, Vec3 point, Vec3 velocity) {
            level.sendParticles(
                    EnderniumParticles.ENDERNIUM_BLESSING_BIT.get(),
                    true,
                    true,
                    point.x, point.y, point.z,
                    0,
                    velocity.x, velocity.y, velocity.z,
                    1.0
            );
        }

        private double noise(int stream, int salt) {
            double value = Math.sin(
                    phase * 17.0
                            + recipientIndex * 91.73
                            + stream * 38.21
                            + salt * 17.17
                            + age * 11.91
            ) * 43758.5453;
            return value - Math.floor(value);
        }

        private static double smoothStep(double value) {
            return value * value * (3.0 - 2.0 * value);
        }

    }
}
