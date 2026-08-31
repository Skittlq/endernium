package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.attachment.ModAttachments;
import com.skittlq.endernium.config.EnderniumConfig;
import com.skittlq.endernium.config.EnderniumConfigManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.entity.LivingEntity;

public final class EnderniumArmorAbilityHandler {
    private static final EnderniumArmorAbility.Settings SETTINGS = new EnderniumArmorAbility.Settings() {
        @Override
        public boolean enabled() {
            return config().enderniumArmorAbility;
        }

        @Override
        public int threshold() {
            return config().enderniumArmorAbilityThreshold;
        }

        @Override
        public long cooldownSeconds() {
            return config().enderniumArmorAbilityCooldown;
        }

        private EnderniumConfig config() {
            return EnderniumConfigManager.getConfig();
        }
    };

    private static final EnderniumArmorAbility.CooldownStore COOLDOWN_STORE = new EnderniumArmorAbility.CooldownStore() {
        @Override
        public long getLastUsedTick(LivingEntity entity) {
            return entity.getAttachedOrCreate(ModAttachments.ENDERNIUM_ARMOR_LAST_USED_TICK);
        }

        @Override
        public void setLastUsedTick(LivingEntity entity, long tick) {
            entity.setAttached(ModAttachments.ENDERNIUM_ARMOR_LAST_USED_TICK, tick);
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
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            EnderniumArmorAbility.tickPlayers(server.getPlayerList().getPlayers(), SETTINGS, COOLDOWN_STORE);
            EnderniumArmorAbility.tickMobs(server.getAllLevels(), SETTINGS, COOLDOWN_STORE);
        });
    }
}
