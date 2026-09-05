package com.skittlq.endernium.item.tools;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class EnderniumSwordCooldownHandler {
    private static final String COOLDOWN_KEY = "EnderniumSwordCooldownEndTick";
    private static final String DURATION_KEY = "EnderniumSwordCooldownDurationTicks";

    private static boolean registered;

    private EnderniumSwordCooldownHandler() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        EnderniumSword.bindCooldownStore(new EnderniumSword.CooldownStore() {
            @Override
            public long getCooldownEndGameTime(Player player) {
                return player.getPersistentData().getLong(COOLDOWN_KEY).orElse(0L);
            }

            @Override
            public void setCooldownEndGameTime(Player player, long gameTime) {
                player.getPersistentData().putLong(COOLDOWN_KEY, gameTime);
            }

            @Override
            public int getCooldownDurationTicks(Player player) {
                return player.getPersistentData().getInt(DURATION_KEY).orElse(0);
            }

            @Override
            public void setCooldownDurationTicks(Player player, int durationTicks) {
                player.getPersistentData().putInt(DURATION_KEY, durationTicks);
            }
        });
        NeoForge.EVENT_BUS.addListener(EnderniumSwordCooldownHandler::onPlayerJoin);
    }

    private static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EnderniumSword.syncCooldownOnLogin(player);
        }
    }
}
