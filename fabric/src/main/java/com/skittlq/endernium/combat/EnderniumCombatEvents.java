package com.skittlq.endernium.combat;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

public final class EnderniumCombatEvents {
    private EnderniumCombatEvents() {
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (!blocked
                    && damageTaken > 0.0F
                    && entity instanceof ServerPlayer victim
                    && source.getEntity() instanceof ServerPlayer attacker) {
                EnderniumCombatTags.recordSuccessfulHit(attacker, victim);
            }
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer player) {
                EnderniumCombatTags.playerDied(player);
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                EnderniumCombatTags.playerJoined(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                EnderniumCombatTags.playerDisconnected(handler.getPlayer()));
        ServerTickEvents.END_SERVER_TICK.register(EnderniumCombatTags::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(EnderniumCombatTags::clear);
    }
}
