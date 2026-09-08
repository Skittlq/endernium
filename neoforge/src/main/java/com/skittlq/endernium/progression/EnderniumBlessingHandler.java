package com.skittlq.endernium.progression;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class EnderniumBlessingHandler {
    private static final String BLESSED_KEY = "EnderniumAbilitiesAwakened";
    private static boolean registered;

    private EnderniumBlessingHandler() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        EnderniumBlessing.bindUnlockStore(new EnderniumBlessing.UnlockStore() {
            @Override
            public boolean isBlessed(ServerPlayer player) {
                return player.getPersistentData().getBoolean(BLESSED_KEY).orElse(false);
            }

            @Override
            public void setBlessed(ServerPlayer player, boolean blessed) {
                player.getPersistentData().putBoolean(BLESSED_KEY, blessed);
            }
        });
        NeoForge.EVENT_BUS.addListener(EnderniumBlessingHandler::onPlayerJoin);
        NeoForge.EVENT_BUS.addListener(EnderniumBlessingHandler::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(EnderniumBlessingHandler::onPlayerRespawn);
    }

    private static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EnderniumBlessing.syncOnLogin(player);
        }
    }

    private static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer original
                && event.getEntity() instanceof ServerPlayer replacement
                && original.getPersistentData().getBoolean(BLESSED_KEY).orElse(false)) {
            replacement.getPersistentData().putBoolean(BLESSED_KEY, true);
        }
    }

    private static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EnderniumBlessing.syncOnLogin(player);
        }
    }
}
