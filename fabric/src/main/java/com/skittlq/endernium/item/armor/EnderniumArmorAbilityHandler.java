package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.attachment.ModAttachments;
import com.skittlq.endernium.config.EnderniumConfig;
import com.skittlq.endernium.config.EnderniumConfigManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
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
        public double maxStoredDamage() {
            return config().enderniumArmorMaxStoredDamage;
        }

        private EnderniumConfig config() {
            return EnderniumConfigManager.getConfig();
        }
    };

    private static final EnderniumArmorAbility.ChargeStore CHARGE_STORE = new EnderniumArmorAbility.ChargeStore() {
        @Override
        public float getStoredDamage(LivingEntity entity) {
            return entity.getAttachedOrCreate(ModAttachments.ENDERNIUM_ARMOR_STORED_DAMAGE);
        }

        @Override
        public void setStoredDamage(LivingEntity entity, float storedDamage) {
            entity.setAttached(ModAttachments.ENDERNIUM_ARMOR_STORED_DAMAGE, storedDamage);
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
        EnderniumArmorAbility.bind(SETTINGS, CHARGE_STORE);
        ServerTickEvents.END_SERVER_TICK.register(server ->
                EnderniumArmorAbility.tickPlayers(server.getPlayerList().getPlayers(), SETTINGS, CHARGE_STORE));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                EnderniumArmorAbility.syncCharge(handler.getPlayer(), SETTINGS, CHARGE_STORE));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                EnderniumArmorAbility.syncCharge(newPlayer, SETTINGS, CHARGE_STORE));
    }
}
