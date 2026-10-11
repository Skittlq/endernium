package com.skittlq.endernium.util;

import com.skittlq.endernium.Endernium;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Endernium.MODID)
public final class EnderniumUtilsEvents {
    private EnderniumUtilsEvents() {
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
            EnderniumUtils.cancelAllVeinMiningOperations(player);
        }
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player player)) {
            return;
        }

        EnderniumUtils.onAutoCollectToolBlockBreak(
                event.getLevel(),
                player,
                event.getPos().immutable(),
                event.getState(),
                event.getDrops(),
                false
        );
    }
}
