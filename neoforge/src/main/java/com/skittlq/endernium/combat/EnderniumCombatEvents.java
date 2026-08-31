package com.skittlq.endernium.combat;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class EnderniumCombatEvents {
    private EnderniumCombatEvents() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EnderniumCombatEvents::onDamage);
        NeoForge.EVENT_BUS.addListener(EnderniumCombatEvents::onDeath);
        NeoForge.EVENT_BUS.addListener(EnderniumCombatEvents::onPlayerJoin);
        NeoForge.EVENT_BUS.addListener(EnderniumCombatEvents::onPlayerLogout);
        NeoForge.EVENT_BUS.addListener(EnderniumCombatEvents::onServerTick);
        NeoForge.EVENT_BUS.addListener(EnderniumCombatEvents::onServerStopped);
    }

    private static void onDamage(LivingDamageEvent.Post event) {
        if (event.getBlockedDamage() <= 0.0F
                && event.getInflictedDamage() > 0.0F
                && event.getEntity() instanceof ServerPlayer victim
                && event.getSource().getEntity() instanceof ServerPlayer attacker) {
            EnderniumCombatTags.recordSuccessfulHit(attacker, victim);
        }
    }

    private static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EnderniumCombatTags.playerDied(player);
        }
    }

    private static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EnderniumCombatTags.playerJoined(player);
        }
    }

    private static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EnderniumCombatTags.playerDisconnected(player);
        }
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        EnderniumCombatTags.tick(event.getServer());
    }

    private static void onServerStopped(ServerStoppedEvent event) {
        EnderniumCombatTags.clear(event.getServer());
    }
}
