package com.skittlq.endernium.progression;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class EnderniumAwakeningHandler {
    private static final String AWAKENED_KEY = "EnderniumAbilitiesAwakened";
    private static boolean registered;

    private EnderniumAwakeningHandler() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        EnderniumAwakening.bindUnlockStore(new EnderniumAwakening.UnlockStore() {
            @Override
            public boolean isAwakened(ServerPlayer player) {
                return player.getPersistentData().getBoolean(AWAKENED_KEY).orElse(false);
            }

            @Override
            public void setAwakened(ServerPlayer player, boolean awakened) {
                player.getPersistentData().putBoolean(AWAKENED_KEY, awakened);
            }
        });
        NeoForge.EVENT_BUS.addListener(EnderniumAwakeningHandler::onPlayerJoin);
        NeoForge.EVENT_BUS.addListener(EnderniumAwakeningHandler::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(EnderniumAwakeningHandler::onPlayerRespawn);
    }

    private static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EnderniumAwakening.syncOnLogin(player);
        }
    }

    private static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer original
                && event.getEntity() instanceof ServerPlayer replacement
                && original.getPersistentData().getBoolean(AWAKENED_KEY).orElse(false)) {
            replacement.getPersistentData().putBoolean(AWAKENED_KEY, true);
        }
    }

    private static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EnderniumAwakening.syncOnLogin(player);
        }
    }
}
