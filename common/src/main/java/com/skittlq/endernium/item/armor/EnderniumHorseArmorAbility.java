package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.item.EnderniumItems;
import com.skittlq.endernium.network.EnderniumNetworking;
import com.skittlq.endernium.particles.EnderniumParticles;
import com.skittlq.endernium.progression.EnderniumBlessing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.HashSet;
import java.util.Set;

/** Movement abilities granted to a ridden horse by Endernium horse armor. */
public final class EnderniumHorseArmorAbility {
    public static final float STEP_HEIGHT = 2.0F;
    public static final double DASH_DISTANCE = 8.0;
    public static final int DASH_COOLDOWN_TICKS = 5 * 20;
    private static final double DASH_SEARCH_STEP = 0.25;
    private static final double MINIMUM_DASH_DISTANCE = 0.75;
    private static final double MAX_LANDING_RISE = 2.0;
    private static final double MAX_LANDING_DROP = 4.0;
    private static final double LANDING_SEARCH_STEP = 0.125;
    private static final Map<net.minecraft.server.MinecraftServer, Map<UUID, Long>> COOLDOWN_END_TICKS =
            new WeakHashMap<>();
    private static final Map<MinecraftServer, Map<UUID, MountedPosition>> LAST_MOUNTED_POSITIONS =
            new WeakHashMap<>();

    private EnderniumHorseArmorAbility() {
    }

    public static boolean isWearingEnderniumArmor(AbstractHorse horse) {
        return horse.getItemBySlot(EquipmentSlot.BODY).is(EnderniumItems.ENDERNIUM_HORSE_ARMOR.get());
    }

    public static boolean hasArmoredHorseMount(Player player) {
        return player.getVehicle() instanceof AbstractHorse horse
                && horse.getControllingPassenger() == player
                && isWearingEnderniumArmor(horse);
    }

    public static boolean canStepUp(AbstractHorse horse) {
        return isWearingEnderniumArmor(horse)
                && horse.getControllingPassenger() instanceof Player rider
                && EnderniumBlessing.isBlessed(rider);
    }

    public static boolean tryDash(Level level, Player player) {
        if (!(level instanceof ServerLevel serverLevel)
                || !(player.getVehicle() instanceof AbstractHorse horse)
                || horse.getControllingPassenger() != player
                || !isWearingEnderniumArmor(horse)
                || !EnderniumBlessing.isBlessed(player)) {
            return false;
        }

        Map<UUID, Long> serverCooldowns = COOLDOWN_END_TICKS.computeIfAbsent(
                serverLevel.getServer(), ignored -> new HashMap<>());
        long gameTime = serverLevel.getGameTime();
        if (serverCooldowns.getOrDefault(player.getUUID(), 0L) > gameTime) {
            return true;
        }

        Vec3 forward = Vec3.directionFromRotation(0.0F, horse.getYRot()).multiply(1.0, 0.0, 1.0).normalize();
        Vec3 origin = horse.position();
        Vec3 destination = findDestination(serverLevel, horse, player, origin, forward);
        if (destination == null) {
            return true;
        }

        long cooldownEndTick = gameTime + DASH_COOLDOWN_TICKS;
        serverCooldowns.put(player.getUUID(), cooldownEndTick);
        EnderniumNetworking.sendHorseCooldownSync((net.minecraft.server.level.ServerPlayer) player,
                cooldownEndTick, DASH_COOLDOWN_TICKS);

        serverLevel.playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1.0F, 0.9F);
        serverLevel.sendParticles(EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(),
                origin.x, origin.y + horse.getBbHeight() * 0.5, origin.z,
                28, 0.45, 0.65, 0.45, 0.15);

        horse.teleportTo(destination.x, destination.y, destination.z);
        horse.setDeltaMovement(Vec3.ZERO);
        horse.fallDistance = 0.0;
        ((net.minecraft.server.level.ServerPlayer) player).connection.send(
                ClientboundMoveVehiclePacket.fromEntity(horse));
        rememberMountedPosition(serverLevel.getServer(), (ServerPlayer) player, horse);

        serverLevel.playSound(null, destination.x, destination.y, destination.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1.0F, 1.15F);
        serverLevel.sendParticles(EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(),
                destination.x, destination.y + horse.getBbHeight() * 0.5, destination.z,
                36, 0.55, 0.7, 0.55, 0.18);
        return true;
    }

    public static void playStepTeleportEffect(AbstractHorse horse) {
        if (!(horse.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.playSound(null, horse.getX(), horse.getY(), horse.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 0.35F, 1.3F);
        serverLevel.sendParticles(EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(),
                horse.getX(), horse.getY() + 0.2, horse.getZ(),
                14, 0.4, 0.18, 0.4, 0.08);
    }

    public static void tick(MinecraftServer server) {
        Map<UUID, MountedPosition> positions = LAST_MOUNTED_POSITIONS.computeIfAbsent(
                server, ignored -> new HashMap<>());
        Set<UUID> activeRiders = new HashSet<>();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!(player.getVehicle() instanceof AbstractHorse horse) || !canStepUp(horse)) {
                positions.remove(player.getUUID());
                continue;
            }

            UUID playerId = player.getUUID();
            activeRiders.add(playerId);
            Vec3 current = horse.position();
            MountedPosition previous = positions.put(
                    playerId, new MountedPosition(horse.getUUID(), current));
            if (previous == null || !previous.horseId().equals(horse.getUUID())) {
                continue;
            }

            double rise = current.y - previous.position().y;
            double horizontalDistanceSqr = new Vec3(
                    current.x - previous.position().x,
                    0.0,
                    current.z - previous.position().z
            ).lengthSqr();
            if (rise > 1.0
                    && rise <= STEP_HEIGHT + 0.25
                    && horizontalDistanceSqr > 1.0E-4
                    && horizontalDistanceSqr < 9.0) {
                playStepTeleportEffect(horse);
            }
        }

        positions.keySet().retainAll(activeRiders);
        if (positions.isEmpty()) {
            LAST_MOUNTED_POSITIONS.remove(server);
        }
    }

    private static void rememberMountedPosition(
            MinecraftServer server,
            ServerPlayer player,
            AbstractHorse horse
    ) {
        LAST_MOUNTED_POSITIONS.computeIfAbsent(server, ignored -> new HashMap<>())
                .put(player.getUUID(), new MountedPosition(horse.getUUID(), horse.position()));
    }

    private static Vec3 findDestination(
            ServerLevel level,
            AbstractHorse horse,
            Player rider,
            Vec3 origin,
            Vec3 forward
    ) {
        AABB bounds = horse.getBoundingBox();
        for (double distance = DASH_DISTANCE; distance >= MINIMUM_DASH_DISTANCE; distance -= DASH_SEARCH_STEP) {
            Vec3 offset = forward.scale(distance);
            double targetX = origin.x + offset.x;
            double targetZ = origin.z + offset.z;
            BlockPos targetBlock = BlockPos.containing(targetX, origin.y, targetZ);
            if (!level.getChunkSource().hasChunk(
                    SectionPos.blockToSectionCoord(targetBlock.getX()),
                    SectionPos.blockToSectionCoord(targetBlock.getZ())
            ) || !level.getWorldBorder().isWithinBounds(targetBlock)) {
                continue;
            }
            for (double verticalOffset = MAX_LANDING_RISE;
                 verticalOffset >= -MAX_LANDING_DROP;
                 verticalOffset -= LANDING_SEARCH_STEP) {
                AABB candidateBounds = bounds.move(offset.x, verticalOffset, offset.z);
                if (level.noCollision(horse, candidateBounds)
                        && level.noCollision(rider,
                        rider.getBoundingBox().move(offset.x, verticalOffset, offset.z))
                        && !level.noCollision(horse, candidateBounds.move(0.0, -LANDING_SEARCH_STEP, 0.0))) {
                    return new Vec3(targetX, origin.y + verticalOffset, targetZ);
                }
            }
        }
        return null;
    }

    public static void clearPlayerState(net.minecraft.server.MinecraftServer server, UUID playerId) {
        Map<UUID, Long> serverCooldowns = COOLDOWN_END_TICKS.get(server);
        if (serverCooldowns != null) {
            serverCooldowns.remove(playerId);
            if (serverCooldowns.isEmpty()) {
                COOLDOWN_END_TICKS.remove(server);
            }
        }
        Map<UUID, MountedPosition> positions = LAST_MOUNTED_POSITIONS.get(server);
        if (positions != null) {
            positions.remove(playerId);
            if (positions.isEmpty()) {
                LAST_MOUNTED_POSITIONS.remove(server);
            }
        }
    }

    public static void clearServerState(net.minecraft.server.MinecraftServer server) {
        COOLDOWN_END_TICKS.remove(server);
        LAST_MOUNTED_POSITIONS.remove(server);
    }

    private record MountedPosition(UUID horseId, Vec3 position) {
    }
}
