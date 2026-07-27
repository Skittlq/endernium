package com.skittlq.endernium.config;

import java.util.Objects;

public final class EnderniumGameplayConfig {
    public static final int DEFAULT_SWORD_ABILITY_BASE_COOLDOWN_SECONDS = 10;
    public static final int DEFAULT_SWORD_ABILITY_PER_MOB_COOLDOWN_SECONDS = 5;

    private static Settings settings = new Settings() {
        @Override
        public boolean swordAbilityEnabled() {
            return true;
        }

        @Override
        public int swordAbilityBaseCooldownSeconds() {
            return DEFAULT_SWORD_ABILITY_BASE_COOLDOWN_SECONDS;
        }

        @Override
        public int swordAbilityPerMobCooldownSeconds() {
            return DEFAULT_SWORD_ABILITY_PER_MOB_COOLDOWN_SECONDS;
        }

        @Override
        public boolean toolsVeinMiningEnabled() {
            return true;
        }
    };

    private EnderniumGameplayConfig() {
    }

    public static void bind(Settings newSettings) {
        settings = Objects.requireNonNull(newSettings);
    }

    public static boolean swordAbilityEnabled() {
        return settings.swordAbilityEnabled();
    }

    public static int swordAbilityBaseCooldownSeconds() {
        return nonNegative(settings.swordAbilityBaseCooldownSeconds());
    }

    public static int swordAbilityPerMobCooldownSeconds() {
        return nonNegative(settings.swordAbilityPerMobCooldownSeconds());
    }

    public static boolean toolsVeinMiningEnabled() {
        return settings.toolsVeinMiningEnabled();
    }

    public static int swordAbilityCooldownTicks(int mobsHit) {
        long seconds = (long) swordAbilityBaseCooldownSeconds()
                + (long) swordAbilityPerMobCooldownSeconds() * Math.max(0, mobsHit);
        return (int) Math.min(Integer.MAX_VALUE, seconds * 20L);
    }

    private static int nonNegative(int value) {
        return Math.max(0, value);
    }

    public interface Settings {
        boolean swordAbilityEnabled();

        int swordAbilityBaseCooldownSeconds();

        int swordAbilityPerMobCooldownSeconds();

        boolean toolsVeinMiningEnabled();
    }
}