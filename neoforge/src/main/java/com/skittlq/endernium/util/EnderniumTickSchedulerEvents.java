package com.skittlq.endernium.util;

import com.skittlq.endernium.Endernium;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;
import com.skittlq.endernium.vfx.DragonDeathVfxTracker;
import com.skittlq.endernium.progression.DragonAwakeningTracker;

@EventBusSubscriber(modid = Endernium.MODID)
public final class EnderniumTickSchedulerEvents {
    private EnderniumTickSchedulerEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        EnderniumTickScheduler.tick(event.getServer());
        DragonDeathVfxTracker.tick(event.getServer());
        DragonAwakeningTracker.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        EnderniumTickScheduler.clear(event.getServer());
        EnderniumUtils.clearServerState(event.getServer());
        com.skittlq.endernium.item.tools.EnderniumSword.clearServerState(event.getServer());
        com.skittlq.endernium.item.EnderniumAbilityHandler.clearServerState(event.getServer());
        DragonDeathVfxTracker.clear();
        DragonAwakeningTracker.clear();
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cancelPlayerState(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            cancelPlayerState(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer player) {
            cancelPlayerState(player);
        }
    }

    private static void cancelPlayerState(ServerPlayer player) {
        com.skittlq.endernium.item.tools.EnderniumSword.cancelSequence(player, false);
        EnderniumUtils.cancelAllVeinMiningOperations(player);
        com.skittlq.endernium.item.EnderniumAbilityHandler.clearPlayerState(
                player.level().getServer(), player.getUUID());
        EnderniumTickScheduler.cancelOwner(player.level().getServer(), player.getUUID());
    }
}
