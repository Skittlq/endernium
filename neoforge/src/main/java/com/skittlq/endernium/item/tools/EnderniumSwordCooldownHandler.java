package com.skittlq.endernium.item.tools;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class EnderniumSwordCooldownHandler {
    private static final String CHARGE_KEY = "EnderniumSwordStoredDamage";

    private static boolean registered;

    private EnderniumSwordCooldownHandler() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        EnderniumSword.bindChargeStore(new EnderniumSword.ChargeStore() {
            @Override
            public float getStoredDamage(Player player) {
                return player.getPersistentData().getFloat(CHARGE_KEY).orElse(0.0F);
            }

            @Override
            public void setStoredDamage(Player player, float storedDamage) {
                player.getPersistentData().putFloat(CHARGE_KEY, storedDamage);
            }
        });
        NeoForge.EVENT_BUS.addListener(EnderniumSwordCooldownHandler::onPlayerJoin);
        NeoForge.EVENT_BUS.addListener(EnderniumSwordCooldownHandler::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(EnderniumSwordCooldownHandler::onPlayerRespawn);
    }

    private static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EnderniumSword.syncCharge(player);
        }
    }

    private static void onPlayerClone(PlayerEvent.Clone event) {
        event.getEntity().getPersistentData().putFloat(CHARGE_KEY, 0.0F);
    }

    private static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EnderniumSword.syncCharge(player);
        }
    }
}
