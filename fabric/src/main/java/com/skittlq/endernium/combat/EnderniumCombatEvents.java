package com.skittlq.endernium.combat;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class EnderniumCombatEvents {
    private EnderniumCombatEvents() {
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            EnderniumCombatHooks.onDamage(entity, source, damageTaken, blocked);
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) ->
                EnderniumCombatHooks.onDeath(entity));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                EnderniumCombatHooks.onPlayerJoin(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                EnderniumCombatHooks.onPlayerDisconnect(handler.getPlayer()));
        ServerTickEvents.END_SERVER_TICK.register(EnderniumCombatHooks::onServerTick);
        ServerLifecycleEvents.SERVER_STOPPED.register(EnderniumCombatHooks::onServerStopped);
    }
}
