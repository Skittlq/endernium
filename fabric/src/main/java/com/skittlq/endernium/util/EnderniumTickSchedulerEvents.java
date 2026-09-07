package com.skittlq.endernium.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import com.skittlq.endernium.vfx.DragonDeathVfxTracker;
import com.skittlq.endernium.progression.DragonAwakeningTracker;

public final class EnderniumTickSchedulerEvents {
    private static boolean registered;

    private EnderniumTickSchedulerEvents() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            EnderniumTickScheduler.tick(server);
            DragonDeathVfxTracker.tick(server);
            DragonAwakeningTracker.tick(server);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            EnderniumTickScheduler.clear(server);
            EnderniumUtils.clearServerState(server);
            com.skittlq.endernium.item.tools.EnderniumSword.clearServerState(server);
            com.skittlq.endernium.item.EnderniumAbilityHandler.clearServerState(server);
            DragonDeathVfxTracker.clear();
            DragonAwakeningTracker.clear();
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                cancelPlayerState(handler.getPlayer()));
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof net.minecraft.server.level.ServerPlayer player) {
                cancelPlayerState(player);
            }
        });
    }

    private static void cancelPlayerState(net.minecraft.server.level.ServerPlayer player) {
        com.skittlq.endernium.item.tools.EnderniumSword.cancelSequence(player, false);
        EnderniumUtils.cancelAllVeinMiningOperations(player);
        com.skittlq.endernium.item.EnderniumAbilityHandler.clearPlayerState(
                player.level().getServer(), player.getUUID());
        EnderniumTickScheduler.cancelOwner(player.level().getServer(), player.getUUID());
    }
}
