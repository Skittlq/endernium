package com.skittlq.endernium.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class EnderniumTickSchedulerEvents {
    private static boolean registered;

    private EnderniumTickSchedulerEvents() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ServerTickEvents.END_SERVER_TICK.register(EnderniumServerLifecycle::onEndTick);
        ServerLifecycleEvents.SERVER_STOPPED.register(EnderniumServerLifecycle::onServerStopped);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                EnderniumServerLifecycle.cancelPlayerState(handler.getPlayer()));
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
                EnderniumServerLifecycle.cancelPlayerState(player);
            }
        });
    }
}
