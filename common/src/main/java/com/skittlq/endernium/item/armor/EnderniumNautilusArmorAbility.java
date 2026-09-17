package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.item.EnderniumItems;
import com.skittlq.endernium.network.EnderniumNetworking;
import com.skittlq.endernium.particles.EnderniumParticles;
import com.skittlq.endernium.progression.EnderniumBlessing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/** Aim-directed underwater teleport granted to a ridden nautilus by Endernium armor. */
public final class EnderniumNautilusArmorAbility {
    public static final double RANGE = 16.0;
    public static final int COOLDOWN_TICKS = 8 * 20;
    private static final double SEARCH_STEP = 0.25;
    private static final double MINIMUM_DISTANCE = 1.0;
    private static final Map<MinecraftServer, Map<UUID, Long>> COOLDOWN_END_TICKS = new WeakHashMap<>();

    private EnderniumNautilusArmorAbility() {
    }

    public static boolean isWearingEnderniumArmor(AbstractNautilus nautilus) {
        return nautilus.getItemBySlot(EquipmentSlot.BODY).is(EnderniumItems.ENDERNIUM_NAUTILUS_ARMOR.get());
    }

    public static boolean hasArmoredNautilusMount(Player player) {
        return player.getVehicle() instanceof AbstractNautilus nautilus
                && nautilus.getControllingPassenger() == player
                && isWearingEnderniumArmor(nautilus);
    }

    public static boolean tryTeleport(Level level, Player player) {
        if (!(level instanceof ServerLevel serverLevel)
                || !(player instanceof ServerPlayer serverPlayer)
                || !(player.getVehicle() instanceof AbstractNautilus nautilus)
                || nautilus.getControllingPassenger() != player
                || !isWearingEnderniumArmor(nautilus)
                || !EnderniumBlessing.isBlessed(player)) {
            return false;
        }

        Map<UUID, Long> serverCooldowns = COOLDOWN_END_TICKS.computeIfAbsent(
                serverLevel.getServer(), ignored -> new HashMap<>());
        long gameTime = serverLevel.getGameTime();
        if (serverCooldowns.getOrDefault(player.getUUID(), 0L) > gameTime) {
            return true;
        }

        Vec3 origin = nautilus.position();
        Vec3 destination = findDestination(serverLevel, nautilus, player);
        if (destination == null) {
            return true;
        }

        long cooldownEndTick = gameTime + COOLDOWN_TICKS;
        serverCooldowns.put(player.getUUID(), cooldownEndTick);
        EnderniumNetworking.sendNautilusCooldownSync(serverPlayer, cooldownEndTick, COOLDOWN_TICKS);

        playTeleportEffect(serverLevel, nautilus, origin, 24);
        spawnTrail(serverLevel, origin.add(0.0, nautilus.getBbHeight() * 0.5, 0.0),
                destination.add(0.0, nautilus.getBbHeight() * 0.5, 0.0));

        nautilus.teleportTo(destination.x, destination.y, destination.z);
        nautilus.setDeltaMovement(Vec3.ZERO);
        nautilus.fallDistance = 0.0;
        serverPlayer.connection.send(ClientboundMoveVehiclePacket.fromEntity(nautilus));

        playTeleportEffect(serverLevel, nautilus, destination, 32);
        return true;
    }

    private static Vec3 findDestination(ServerLevel level, AbstractNautilus nautilus, Player rider) {
        Vec3 origin = nautilus.position();
        Vec3 center = origin.add(0.0, nautilus.getBbHeight() * 0.5, 0.0);
        Vec3 direction = rider.getLookAngle().normalize();
        AABB nautilusBounds = nautilus.getBoundingBox();
        AABB riderBounds = rider.getBoundingBox();
        Vec3 best = null;

        for (double distance = MINIMUM_DISTANCE; distance <= RANGE; distance += SEARCH_STEP) {
            Vec3 candidateCenter = center.add(direction.scale(distance));
            Vec3 candidate = candidateCenter.subtract(0.0, nautilus.getBbHeight() * 0.5, 0.0);
            BlockPos centerBlock = BlockPos.containing(candidateCenter);
            if (!level.getChunkSource().hasChunk(
                    SectionPos.blockToSectionCoord(centerBlock.getX()),
                    SectionPos.blockToSectionCoord(centerBlock.getZ())
            )
                    || !level.getWorldBorder().isWithinBounds(centerBlock)
                    || !level.getFluidState(centerBlock).is(FluidTags.WATER)) {
                break;
            }

            Vec3 offset = candidate.subtract(origin);
            if (!level.noCollision(nautilus, nautilusBounds.move(offset))
                    || !level.noCollision(rider, riderBounds.move(offset))) {
                break;
            }
            best = candidate;
        }
        return best;
    }

    private static void playTeleportEffect(ServerLevel level, AbstractNautilus nautilus, Vec3 position, int count) {
        level.playSound(null, position.x, position.y, position.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 0.75F, 0.85F);
        level.sendParticles(EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(),
                position.x, position.y + nautilus.getBbHeight() * 0.5, position.z,
                count, 0.55, 0.45, 0.55, 0.14);
    }

    private static void spawnTrail(ServerLevel level, Vec3 start, Vec3 end) {
        double distance = start.distanceTo(end);
        int steps = Math.max(1, (int) Math.ceil(distance));
        for (int i = 1; i < steps; i++) {
            Vec3 point = start.lerp(end, (double) i / steps);
            level.sendParticles(EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(),
                    point.x, point.y, point.z, 2, 0.12, 0.12, 0.12, 0.02);
        }
    }

    public static void clearPlayerState(MinecraftServer server, UUID playerId) {
        Map<UUID, Long> serverCooldowns = COOLDOWN_END_TICKS.get(server);
        if (serverCooldowns != null) {
            serverCooldowns.remove(playerId);
            if (serverCooldowns.isEmpty()) {
                COOLDOWN_END_TICKS.remove(server);
            }
        }
    }

    public static void clearServerState(MinecraftServer server) {
        COOLDOWN_END_TICKS.remove(server);
    }
}
