package com.skittlq.endernium.vfx;

import com.skittlq.endernium.network.EnderniumNetworking;
import com.skittlq.endernium.network.payloads.DragonDeathVfxPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Tracks the world's first dragon death without modifying the vanilla fight or reward flow. */
public final class DragonDeathVfxTracker {
    private static final int MIN_AFTERMATH_TICKS = 74;
    private static final int LOOSE_ENTITY_TAIL_TICKS = 32;
    private static final double MAX_OCCLUSION_RAY_DISTANCE = 256.0;
    private static final int MAX_REACTING_ENDERMEN = 100;
    private static Session active;
    private static MinecraftServer activeServer;

    private DragonDeathVfxTracker() {
    }

    public static void tick(MinecraftServer server) {
        if (server != activeServer) {
            active = null;
            activeServer = server;
        }

        ServerLevel end = server.getLevel(Level.END);
        if (end == null) {
            active = null;
            return;
        }

        List<? extends EnderDragon> dragons = end.getEntities(EntityTypes.ENDER_DRAGON, dragon -> true);
        if (active == null) {
            var fight = end.getDragonFight();
            if (fight == null || fight.hasPreviouslyKilledDragon()) {
                return;
            }

            for (EnderDragon dragon : dragons) {
                if (dragon.dragonDeathTime > 0 && !dragon.isRemoved()) {
                    active = Session.start(dragon);
                    active.tickBuildupEndermen(end, dragon);
                    notifyNewPlayers(end, active);
                    return;
                }
            }
            return;
        }

        if (active.burstAge >= 0) {
            active.burstAge++;
            active.tickAftermath(end);
            if (active.burstAge >= active.aftermathEndTick) {
                active = null;
            }
            return;
        }

        if (active.simulated) {
            active.tickSimulation(end);
            return;
        }

        EnderDragon tracked = null;
        for (EnderDragon dragon : dragons) {
            if (dragon.getUUID().equals(active.dragonId)) {
                tracked = dragon;
                break;
            }
        }

        if (tracked != null && !tracked.isRemoved()) {
            active.entityId = tracked.getId();
            active.lastOrigin = tracked.position().add(0.0, 2.0, 0.0);
            active.deathTicks = Math.max(active.deathTicks, tracked.dragonDeathTime);
            active.tickBuildupEndermen(end, tracked);
            notifyNewPlayers(end, active);
            return;
        }

        DragonDeathVfxPayload.Action action = active.deathTicks >= 199
                ? DragonDeathVfxPayload.Action.BURST
                : DragonDeathVfxPayload.Action.CANCEL;
        DragonDeathVfxPayload payload = active.payload(
                action,
                action == DragonDeathVfxPayload.Action.BURST ? 200 : active.deathTicks
        );
        for (ServerPlayer player : end.players()) {
            EnderniumNetworking.sendDragonDeathVfx(player, payload);
        }
        if (action == DragonDeathVfxPayload.Action.BURST) {
            active.beginAftermath(end);
        } else {
            active = null;
        }
    }

    public static void clear() {
        active = null;
        activeServer = null;
    }

    static boolean startSimulation(ServerLevel end, Vec3 origin) {
        MinecraftServer server = end.getServer();
        if (activeServer != null && activeServer != server) {
            active = null;
        }
        activeServer = server;
        if (active != null && !active.simulated) {
            return false;
        }
        if (active != null) {
            sendToEnd(end, active.payload(DragonDeathVfxPayload.Action.CANCEL, active.deathTicks));
        }
        active = Session.simulation(origin);
        active.tickBuildupEndermen(end, origin);
        notifyNewPlayers(end, active);
        return true;
    }

    private static void sendToEnd(ServerLevel end, DragonDeathVfxPayload payload) {
        for (ServerPlayer player : end.players()) {
            EnderniumNetworking.sendDragonDeathVfx(player, payload);
        }
    }

    private static void notifyNewPlayers(ServerLevel end, Session session) {
        for (ServerPlayer player : end.players()) {
            if (session.notifiedPlayers.add(player.getUUID())) {
                EnderniumNetworking.sendDragonDeathVfx(player,
                        session.payload(DragonDeathVfxPayload.Action.START, session.deathTicks));
            }
        }
    }

    private static final class Session {
        private final UUID dragonId;
        private final long sequenceSeed;
        private final boolean simulated;
        private final Set<UUID> notifiedPlayers = new HashSet<>();
        private final Set<UUID> burstPlayers = new HashSet<>();
        private final Map<UUID, Integer> burstPlayerArrivalTicks = new HashMap<>();
        private final Set<UUID> pushedEntities = new HashSet<>();
        private final Set<UUID> watchingEndermen = new HashSet<>();
        private final Map<UUID, Vec3> endermanAnchors = new HashMap<>();
        private final List<EndermanReaction> endermanReactions = new ArrayList<>();
        private int entityId;
        private Vec3 lastOrigin;
        private int deathTicks;
        private int burstAge = -1;
        private int aftermathEndTick = MIN_AFTERMATH_TICKS;

        private Session(
                UUID dragonId,
                int entityId,
                Vec3 lastOrigin,
                int deathTicks,
                long sequenceSeed,
                boolean simulated
        ) {
            this.dragonId = dragonId;
            this.entityId = entityId;
            this.lastOrigin = lastOrigin;
            this.deathTicks = deathTicks;
            this.sequenceSeed = sequenceSeed;
            this.simulated = simulated;
        }

        private static Session start(EnderDragon dragon) {
            UUID id = dragon.getUUID();
            long seed = id.getMostSignificantBits() ^ Long.rotateLeft(id.getLeastSignificantBits(), 23);
            return new Session(
                    id,
                    dragon.getId(),
                    dragon.position().add(0.0, 2.0, 0.0),
                    dragon.dragonDeathTime,
                    seed,
                    false
            );
        }

        private static Session simulation(Vec3 origin) {
            long seed = 0x5EED_DA7A_2026L
                    ^ Double.doubleToLongBits(origin.x)
                    ^ Long.rotateLeft(Double.doubleToLongBits(origin.y), 17)
                    ^ Long.rotateLeft(Double.doubleToLongBits(origin.z), 41);
            return new Session(UUID.randomUUID(), -1, origin, 0, seed, true);
        }

        private DragonDeathVfxPayload payload(DragonDeathVfxPayload.Action action, int ticks) {
            return new DragonDeathVfxPayload(action, dragonId, entityId, lastOrigin, ticks, sequenceSeed);
        }

        private void tickSimulation(ServerLevel end) {
            deathTicks = Math.min(200, deathTicks + 1);
            tickBuildupEndermen(end, lastOrigin);
            notifyNewPlayers(end, this);
            if (deathTicks < 200) {
                return;
            }
            sendToEnd(end, payload(DragonDeathVfxPayload.Action.BURST, 200));
            beginAftermath(end);
        }

        private void beginAftermath(ServerLevel end) {
            burstAge = 0;
            for (ServerPlayer player : end.players()) {
                burstPlayers.add(player.getUUID());
                int arrivalTick = DragonDeathVfxTiming.waveArrivalTick(lastOrigin, player.position());
                burstPlayerArrivalTicks.put(player.getUUID(), arrivalTick);
                aftermathEndTick = Math.max(
                        aftermathEndTick,
                        arrivalTick + LOOSE_ENTITY_TAIL_TICKS
                );
            }

            List<EnderMan> candidates = new ArrayList<>();
            for (UUID id : watchingEndermen) {
                Entity entity = end.getEntity(id);
                if (entity instanceof EnderMan enderman && enderman.isAlive()) {
                    candidates.add(enderman);
                }
            }
            if (candidates.size() < MAX_REACTING_ENDERMEN) {
                for (EnderMan enderman : end.getEntities(EntityTypes.ENDERMAN, EnderMan::isAlive)) {
                    if (!watchingEndermen.contains(enderman.getUUID())) {
                        candidates.add(enderman);
                    }
                }
            }
            candidates.sort(Comparator.comparingLong(enderman -> entityHash(sequenceSeed, enderman.getUUID())));
            int count = Math.min(MAX_REACTING_ENDERMEN, candidates.size());
            for (int i = 0; i < count; i++) {
                EnderMan enderman = candidates.get(i);
                long hash = entityHash(sequenceSeed ^ 0x454E4445524D414EL, enderman.getUUID());
                int arrival = Math.max(
                        1,
                        DragonDeathVfxTiming.waveArrivalTick(lastOrigin, enderman.position())
                );
                int teleportAge = arrival + 6 + Math.floorMod((int)hash, 7);
                aftermathEndTick = Math.max(aftermathEndTick, teleportAge + 1);
                endermanReactions.add(new EndermanReaction(
                        enderman.getUUID(),
                        hash,
                        teleportAge,
                        endermanAnchors.getOrDefault(enderman.getUUID(), enderman.position())
                ));
            }
        }

        private void tickBuildupEndermen(ServerLevel end, EnderDragon dragon) {
            tickBuildupEndermen(
                    end,
                    dragon.position().add(0.0, dragon.getBbHeight() * 0.55, 0.0)
            );
        }

        private void tickBuildupEndermen(ServerLevel end, Vec3 focus) {
            watchingEndermen.removeIf(id -> {
                Entity entity = end.getEntity(id);
                boolean remove = !(entity instanceof EnderMan enderman) || !enderman.isAlive();
                if (remove) {
                    endermanAnchors.remove(id);
                }
                return remove;
            });
            if (watchingEndermen.size() < MAX_REACTING_ENDERMEN) {
                List<? extends EnderMan> candidates = new ArrayList<>(end.getEntities(EntityTypes.ENDERMAN, EnderMan::isAlive));
                candidates.sort(Comparator
                        .comparingDouble((EnderMan enderman) -> horizontalDistanceSqr(enderman.position(), lastOrigin))
                        .thenComparingLong(enderman -> entityHash(sequenceSeed, enderman.getUUID())));
                for (EnderMan enderman : candidates) {
                    if (watchingEndermen.size() >= MAX_REACTING_ENDERMEN) {
                        break;
                    }
                    if (watchingEndermen.add(enderman.getUUID())) {
                        endermanAnchors.put(enderman.getUUID(), enderman.position());
                    }
                }
            }

            for (UUID id : watchingEndermen) {
                Entity entity = end.getEntity(id);
                if (entity instanceof EnderMan enderman && enderman.isAlive()) {
                    holdEnderman(
                            enderman,
                            endermanAnchors.getOrDefault(id, enderman.position()),
                            focus
                    );
                }
            }
        }

        private void tickAftermath(ServerLevel end) {
            pushPlayers(end);
            for (ItemEntity item : end.getEntities(EntityTypes.ITEM, ItemEntity::isAlive)) {
                pushLooseEntity(end, item);
            }
            tickEndermen(end);
        }

        private void pushPlayers(ServerLevel end) {
            for (ServerPlayer player : end.players()) {
                if (!burstPlayers.contains(player.getUUID())
                        || pushedEntities.contains(player.getUUID())
                        || player.isSpectator()) {
                    continue;
                }
                Integer arrivalTick = burstPlayerArrivalTicks.get(player.getUUID());
                if (arrivalTick == null || burstAge < arrivalTick) {
                    continue;
                }
                double exposureScale = isBlastExposed(end, player) ? 1.0 : 0.25;
                Vec3 direction = radialOutwardDirection(
                        player,
                        sequenceSeed ^ player.getUUID().getLeastSignificantBits()
                );
                player.push(
                        direction.x * 5.0 * exposureScale,
                        direction.y * 5.0 * exposureScale,
                        direction.z * 5.0 * exposureScale
                );
                player.hurtMarked = true;
                pushedEntities.add(player.getUUID());
            }
        }

        private void pushLooseEntity(ServerLevel end, Entity entity) {
            if (pushedEntities.contains(entity.getUUID())) {
                return;
            }
            if (burstAge < DragonDeathVfxTiming.waveArrivalTick(lastOrigin, entity.position())) {
                return;
            }
            double exposureScale = isBlastExposed(end, entity) ? 1.0 : 0.25;
            Vec3 direction = radialOutwardDirection(
                    entity,
                    sequenceSeed ^ entity.getUUID().getMostSignificantBits()
            );
            entity.push(
                    direction.x * 5.0 * exposureScale,
                    direction.y * 5.0 * exposureScale,
                    direction.z * 5.0 * exposureScale
            );
            entity.hurtMarked = true;
            pushedEntities.add(entity.getUUID());
        }


        private boolean isBlastExposed(ServerLevel end, Entity entity) {
            AABB bounds = entity.getBoundingBox();
            double sampleInset = Math.min(0.15, bounds.getYsize() * 0.2);
            double lowerY = bounds.minY + sampleInset;
            double upperY = bounds.maxY - sampleInset;
            double centerY = (bounds.minY + bounds.maxY) * 0.5;
            double centerX = (bounds.minX + bounds.maxX) * 0.5;
            double centerZ = (bounds.minZ + bounds.maxZ) * 0.5;

            return hasClearBlastPath(end, entity, new Vec3(centerX, centerY, centerZ))
                    || hasClearBlastPath(end, entity, new Vec3(centerX, lowerY, centerZ))
                    || hasClearBlastPath(end, entity, new Vec3(centerX, upperY, centerZ));
        }

        private boolean hasClearBlastPath(ServerLevel end, Entity entity, Vec3 target) {
            Vec3 rayStart = lastOrigin;
            Vec3 path = target.subtract(lastOrigin);
            if (path.lengthSqr() > MAX_OCCLUSION_RAY_DISTANCE * MAX_OCCLUSION_RAY_DISTANCE) {
                rayStart = target.subtract(path.normalize().scale(MAX_OCCLUSION_RAY_DISTANCE));
            }
            HitResult hit = end.clip(new ClipContext(
                    rayStart,
                    target,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    entity
            ));
            return hit.getType() == HitResult.Type.MISS;
        }

        private void tickEndermen(ServerLevel end) {
            for (EndermanReaction reaction : endermanReactions) {
                if (reaction.teleported || burstAge > reaction.teleportAge) {
                    continue;
                }
                Entity entity = end.getEntity(reaction.endermanId);
                if (!(entity instanceof EnderMan enderman) || !enderman.isAlive()) {
                    continue;
                }
                holdEnderman(enderman, reaction.anchor, lastOrigin);
                if (!reaction.teleported && burstAge >= reaction.teleportAge) {
                    reaction.teleported = true;
                    tryScatterTeleport(end, enderman, reaction.seed);
                }
            }
        }

        private void tryScatterTeleport(ServerLevel end, EnderMan enderman, long seed) {
            Vec3 outward = horizontalOutwardDirection(enderman.position(), seed);
            double baseAngle = Math.atan2(outward.z, outward.x);
            for (int attempt = 0; attempt < 8; attempt++) {
                long attemptSeed = mixSeed(seed + attempt * 0x9E3779B97F4A7C15L);
                double angleOffset = (((attemptSeed >>> 11) & 0xFFFFL) / 65535.0 - 0.5) * Math.toRadians(120.0);
                double distance = 6.0 + ((attemptSeed >>> 29) & 0xFFFFL) / 65535.0 * 6.0;
                int x = Mth.floor(enderman.getX() + Math.cos(baseAngle + angleOffset) * distance);
                int z = Mth.floor(enderman.getZ() + Math.sin(baseAngle + angleOffset) * distance);
                int startY = Mth.floor(enderman.getY()) + 4;
                for (int y = startY; y >= startY - 10; y--) {
                    BlockPos feet = new BlockPos(x, y, z);
                    if (!end.hasChunkAt(feet) || !isSafeStandingSpace(end, feet)) {
                        continue;
                    }
                    if (enderman.randomTeleport(x + 0.5, y, z + 0.5, true)) {
                        return;
                    }
                }
            }
        }

        private Vec3 radialOutwardDirection(Entity entity, long fallbackSeed) {
            Vec3 center = entity.position().add(0.0, entity.getBbHeight() * 0.5, 0.0);
            Vec3 direction = center.subtract(lastOrigin);
            if (direction.lengthSqr() > 1.0E-6) {
                return direction.normalize();
            }
            return horizontalOutwardDirection(center, fallbackSeed);
        }

        private Vec3 horizontalOutwardDirection(Vec3 position, long fallbackSeed) {
            double dx = position.x - lastOrigin.x;
            double dz = position.z - lastOrigin.z;
            double length = Math.sqrt(dx * dx + dz * dz);
            if (length > 1.0E-4) {
                return new Vec3(dx / length, 0.0, dz / length);
            }
            double angle = (mixSeed(fallbackSeed) >>> 11) * 0x1.0p-53 * Math.PI * 2.0;
            return new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
        }

        private void holdEnderman(EnderMan enderman, Vec3 anchor, Vec3 focus) {
            enderman.getNavigation().stop();
            enderman.setSpeed(0.0F);
            Vec3 movement = enderman.getDeltaMovement();
            enderman.setDeltaMovement(0.0, movement.y, 0.0);
            enderman.setPos(anchor.x, enderman.getY(), anchor.z);
            enderman.xo = anchor.x;
            enderman.zo = anchor.z;
            enderman.getLookControl().setLookAt(focus.x, focus.y, focus.z, 180.0F, 180.0F);
            double dx = focus.x - enderman.getX();
            double dy = focus.y - enderman.getEyeY();
            double dz = focus.z - enderman.getZ();
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            float yaw = (float)(Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
            float pitch = (float)(-Math.atan2(dy, horizontal) * 180.0 / Math.PI);
            enderman.setYRot(yaw);
            enderman.setYHeadRot(yaw);
            enderman.setYBodyRot(yaw);
            enderman.setXRot(pitch);

            // AI look goals already ran this tick. Match the interpolation origins as well as the
            // current pose so the client never blends from the discarded "look away" rotation.
            enderman.yRotO = yaw;
            enderman.yHeadRotO = yaw;
            enderman.yBodyRotO = yaw;
            enderman.xRotO = pitch;
        }
    }

    private static boolean isSafeStandingSpace(ServerLevel level, BlockPos feet) {
        BlockPos floor = feet.below();
        BlockState floorState = level.getBlockState(floor);
        return floorState.isFaceSturdy(level, floor, Direction.UP)
                && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getFluidState(feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                && level.getFluidState(feet.above()).isEmpty();
    }

    private static double horizontalDistance(Vec3 a, Vec3 b) {
        return Math.sqrt(horizontalDistanceSqr(a, b));
    }

    private static double horizontalDistanceSqr(Vec3 a, Vec3 b) {
        double dx = a.x - b.x;
        double dz = a.z - b.z;
        return dx * dx + dz * dz;
    }

    private static long entityHash(long seed, UUID id) {
        return mixSeed(seed ^ id.getMostSignificantBits() ^ Long.rotateLeft(id.getLeastSignificantBits(), 21));
    }

    private static long mixSeed(long value) {
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    private static final class EndermanReaction {
        private final UUID endermanId;
        private final long seed;
        private final int teleportAge;
        private final Vec3 anchor;
        private boolean teleported;

        private EndermanReaction(UUID endermanId, long seed, int teleportAge, Vec3 anchor) {
            this.endermanId = endermanId;
            this.seed = seed;
            this.teleportAge = teleportAge;
            this.anchor = anchor;
        }
    }
}
