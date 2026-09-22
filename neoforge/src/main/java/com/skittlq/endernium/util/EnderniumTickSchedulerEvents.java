package com.skittlq.endernium.util;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.item.tools.EnderniumSpearPendingReturns;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;

@EventBusSubscriber(modid = Endernium.MODID)
public final class EnderniumTickSchedulerEvents {
    private EnderniumTickSchedulerEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        EnderniumServerLifecycle.onEndTick(event.getServer());
        EnderniumSpearPendingReturns.onServerTick(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        EnderniumServerLifecycle.onServerStopped(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EnderniumSpearPendingReturns.onDisconnect(player);
            EnderniumServerLifecycle.cancelPlayerState(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EnderniumSpearPendingReturns.onLogin(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EnderniumServerLifecycle.cancelPlayerState(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer player) {
            EnderniumServerLifecycle.cancelPlayerState(player);
        }
    }
}
