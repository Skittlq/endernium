package com.skittlq.endernium.util;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class EnderniumUtilsEvents {
    private static final double DROP_COLLECTION_RADIUS = 1.25D;
    private static final Map<UUID, BreakSnapshot> BREAK_SNAPSHOTS = new HashMap<>();
    private static boolean registered;

    private EnderniumUtilsEvents() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            if (level instanceof ServerLevel serverLevel && EnderniumUtils.isAutoCollectEligible(player)) {
                long gameTime = serverLevel.getGameTime();
                BREAK_SNAPSHOTS.entrySet().removeIf(entry -> entry.getValue().gameTime() + 2L < gameTime);
                BREAK_SNAPSHOTS.put(player.getUUID(), new BreakSnapshot(
                        pos.immutable(), gameTime, nearbyDropIds(serverLevel, pos)
                ));
            }
            return true;
        });
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            BreakSnapshot snapshot = BREAK_SNAPSHOTS.remove(player.getUUID());
            if (!(level instanceof ServerLevel serverLevel)
                    || snapshot == null
                    || !snapshot.pos().equals(pos)) {
                return;
            }

            boolean allowVeinMiningFallback = !EnderniumUtils.isBreakingAdditionalBlock(player);
            EnderniumTickScheduler.schedule(serverLevel.getServer(), player.getUUID(), () -> {
                if (player.isRemoved()) {
                    return;
                }
                List<ItemEntity> drops = nearbyDrops(serverLevel, pos).stream()
                        .filter(drop -> !snapshot.existingDropIds().contains(drop.getUUID()))
                        .toList();
                EnderniumUtils.onAutoCollectToolBlockBreak(
                        serverLevel, player, pos, state, drops, allowVeinMiningFallback
                );
            }, 1);
        });
        PlayerBlockBreakEvents.CANCELED.register((level, player, pos, state, blockEntity) ->
                BREAK_SNAPSHOTS.remove(player.getUUID()));
    }

    private static Set<UUID> nearbyDropIds(ServerLevel level, BlockPos pos) {
        Set<UUID> ids = new HashSet<>();
        for (ItemEntity item : nearbyDrops(level, pos)) {
            ids.add(item.getUUID());
        }
        return ids;
    }

    private static List<ItemEntity> nearbyDrops(ServerLevel level, BlockPos pos) {
        AABB dropBox = AABB.ofSize(
                Vec3.atCenterOf(pos),
                DROP_COLLECTION_RADIUS * 2.0D,
                DROP_COLLECTION_RADIUS * 2.0D,
                DROP_COLLECTION_RADIUS * 2.0D
        );
        return level.getEntitiesOfClass(ItemEntity.class, dropBox, ItemEntity::isAlive);
    }

    private record BreakSnapshot(BlockPos pos, long gameTime, Set<UUID> existingDropIds) {
    }
}
