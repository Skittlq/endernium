package com.skittlq.endernium.item.tools;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class EnderniumSpearStateHandler {
    private static final String STRAIN_KEY = "EnderniumSpearStrainEndTick";
    private static boolean registered;

    private EnderniumSpearStateHandler() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        EnderniumSpear.bindStrainStore(new EnderniumSpear.StrainStore() {
            @Override
            public long getEndGameTime(Player player) {
                return player.getPersistentData().getLong(STRAIN_KEY).orElse(0L);
            }

            @Override
            public void setEndGameTime(Player player, long gameTime) {
                player.getPersistentData().putLong(STRAIN_KEY, gameTime);
            }
        });
        NeoForge.EVENT_BUS.addListener(EnderniumSpearStateHandler::onPlayerClone);
    }

    private static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getEntity().getPersistentData().putLong(STRAIN_KEY, 0L);
            return;
        }
        long endTick = event.getOriginal().getPersistentData().getLong(STRAIN_KEY).orElse(0L);
        event.getEntity().getPersistentData().putLong(STRAIN_KEY, endTick);
    }
}
