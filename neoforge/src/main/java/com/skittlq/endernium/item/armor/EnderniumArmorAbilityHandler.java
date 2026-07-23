package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.Config;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class EnderniumArmorAbilityHandler {
    private static final String COOLDOWN_KEY = "EnderniumArmorCooldown";

    private static final EnderniumArmorAbility.Settings SETTINGS = new EnderniumArmorAbility.Settings() {
        @Override
        public boolean enabled() {
            return Config.ENDERNIUM_ARMOR_ABILITY.getAsBoolean();
        }

        @Override
        public int threshold() {
            return Config.ENDERNIUM_ARMOR_ABILITY_THRESHOLD.getAsInt();
        }

        @Override
        public long cooldownSeconds() {
            return Config.ENDERNIUM_ARMOR_ABILITY_COOLDOWN.getAsLong();
        }
    };

    private static final EnderniumArmorAbility.CooldownStore COOLDOWN_STORE = new EnderniumArmorAbility.CooldownStore() {
        @Override
        public long getLastUsedTick(ServerPlayer player) {
            return player.getPersistentData().getLong(COOLDOWN_KEY).orElse(0L);
        }

        @Override
        public void setLastUsedTick(ServerPlayer player, long tick) {
            player.getPersistentData().putLong(COOLDOWN_KEY, tick);
        }
    };

    private static boolean registered;

    private EnderniumArmorAbilityHandler() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        NeoForge.EVENT_BUS.addListener(EnderniumArmorAbilityHandler::onServerTick);
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        EnderniumArmorAbility.tickPlayers(event.getServer().getPlayerList().getPlayers(), SETTINGS, COOLDOWN_STORE);
    }
}