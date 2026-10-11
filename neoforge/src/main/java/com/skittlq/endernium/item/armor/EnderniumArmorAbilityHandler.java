package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.Config;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class EnderniumArmorAbilityHandler {
    private static final String CHARGE_KEY = "EnderniumArmorStoredDamage";
    private static final String COOLDOWN_END_TICK_KEY = "EnderniumArmorCooldownEndTick";

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
        public int cooldownSeconds() {
            return Config.ENDERNIUM_ARMOR_ABILITY_COOLDOWN.getAsInt();
        }

        @Override
        public double maxStoredDamage() {
            return Config.ENDERNIUM_ARMOR_MAX_STORED_DAMAGE.getAsDouble();
        }
    };

    private static final EnderniumArmorAbility.CooldownStore COOLDOWN_STORE = new EnderniumArmorAbility.CooldownStore() {
        @Override
        public long getCooldownEndTick(LivingEntity entity) {
            return entity.getPersistentData().getLong(COOLDOWN_END_TICK_KEY).orElse(0L);
        }

        @Override
        public void setCooldownEndTick(LivingEntity entity, long cooldownEndTick) {
            entity.getPersistentData().putLong(COOLDOWN_END_TICK_KEY, cooldownEndTick);
        }
    };

    private static final EnderniumArmorAbility.ChargeStore CHARGE_STORE = new EnderniumArmorAbility.ChargeStore() {
        @Override
        public float getStoredDamage(LivingEntity entity) {
            return entity.getPersistentData().getFloat(CHARGE_KEY).orElse(0.0F);
        }

        @Override
        public void setStoredDamage(LivingEntity entity, float storedDamage) {
            entity.getPersistentData().putFloat(CHARGE_KEY, storedDamage);
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
        EnderniumArmorAbility.bind(SETTINGS, CHARGE_STORE, COOLDOWN_STORE);
        NeoForge.EVENT_BUS.addListener(EnderniumArmorAbilityHandler::onServerTick);
        NeoForge.EVENT_BUS.addListener(EnderniumArmorAbilityHandler::onPlayerJoin);
        NeoForge.EVENT_BUS.addListener(EnderniumArmorAbilityHandler::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(EnderniumArmorAbilityHandler::onPlayerRespawn);
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        EnderniumArmorAbility.tickPlayers(event.getServer().getPlayerList().getPlayers(), SETTINGS,
                CHARGE_STORE, COOLDOWN_STORE);
    }

    private static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EnderniumArmorAbility.syncCharge(player, SETTINGS, CHARGE_STORE);
        }
    }

    private static void onPlayerClone(PlayerEvent.Clone event) {
        event.getEntity().getPersistentData().putFloat(CHARGE_KEY, 0.0F);
        event.getEntity().getPersistentData().putLong(COOLDOWN_END_TICK_KEY, 0L);
    }

    private static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EnderniumArmorAbility.syncCharge(player, SETTINGS, CHARGE_STORE);
        }
    }
}
