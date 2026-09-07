package com.skittlq.endernium.config;

import java.util.Objects;

public final class EnderniumGameplayConfig {
    public static final int DEFAULT_SWORD_ABILITY_BASE_COOLDOWN_SECONDS = 10;
    public static final int DEFAULT_SWORD_ABILITY_PER_MOB_COOLDOWN_SECONDS = 5;
    public static final int DEFAULT_ARMOR_ABILITY_THRESHOLD = 4;
    public static final long DEFAULT_ARMOR_ABILITY_COOLDOWN_SECONDS = 90L;
    public static final int MAX_COOLDOWN_SECONDS = Integer.MAX_VALUE / 20;

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

        @Override
        public boolean armorAbilityEnabled() {
            return true;
        }

        @Override
        public int armorAbilityThreshold() {
            return DEFAULT_ARMOR_ABILITY_THRESHOLD;
        }

        @Override
        public long armorAbilityCooldownSeconds() {
            return DEFAULT_ARMOR_ABILITY_COOLDOWN_SECONDS;
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
        return boundedCooldownSeconds(settings.swordAbilityBaseCooldownSeconds());
    }

    public static int swordAbilityPerMobCooldownSeconds() {
        return boundedCooldownSeconds(settings.swordAbilityPerMobCooldownSeconds());
    }

    public static boolean toolsVeinMiningEnabled() {
        return settings.toolsVeinMiningEnabled();
    }

    public static boolean armorAbilityEnabled() {
        return settings.armorAbilityEnabled();
    }

    public static int armorAbilityThreshold() {
        return Math.max(1, Math.min(2048, settings.armorAbilityThreshold()));
    }

    public static long armorAbilityCooldownSeconds() {
        return Math.max(1L, Math.min(MAX_COOLDOWN_SECONDS, settings.armorAbilityCooldownSeconds()));
    }

    public static int swordAbilityCooldownTicks(int mobsHit) {
        long seconds = (long) swordAbilityBaseCooldownSeconds()
                + (long) swordAbilityPerMobCooldownSeconds() * Math.max(0, mobsHit);
        if (seconds > MAX_COOLDOWN_SECONDS) {
            return Integer.MAX_VALUE;
        }
        return (int) (seconds * 20L);
    }

    private static int boundedCooldownSeconds(int value) {
        return Math.max(0, Math.min(MAX_COOLDOWN_SECONDS, value));
    }

    public interface Settings {
        boolean swordAbilityEnabled();

        int swordAbilityBaseCooldownSeconds();

        int swordAbilityPerMobCooldownSeconds();

        boolean toolsVeinMiningEnabled();

        boolean armorAbilityEnabled();

        int armorAbilityThreshold();

        long armorAbilityCooldownSeconds();
    }

    public record Snapshot(boolean swordAbilityEnabled, int swordBaseCooldownSeconds,
                           int swordPerMobCooldownSeconds, boolean toolsVeinMiningEnabled,
                           boolean armorAbilityEnabled, int armorThreshold,
                           long armorCooldownSeconds) {
        public static Snapshot defaults() {
            return new Snapshot(true,
                    DEFAULT_SWORD_ABILITY_BASE_COOLDOWN_SECONDS,
                    DEFAULT_SWORD_ABILITY_PER_MOB_COOLDOWN_SECONDS,
                    true,
                    true,
                    DEFAULT_ARMOR_ABILITY_THRESHOLD,
                    DEFAULT_ARMOR_ABILITY_COOLDOWN_SECONDS);
        }

        public static Snapshot current() {
            return new Snapshot(EnderniumGameplayConfig.swordAbilityEnabled(),
                    EnderniumGameplayConfig.swordAbilityBaseCooldownSeconds(),
                    EnderniumGameplayConfig.swordAbilityPerMobCooldownSeconds(),
                    EnderniumGameplayConfig.toolsVeinMiningEnabled(),
                    EnderniumGameplayConfig.armorAbilityEnabled(),
                    EnderniumGameplayConfig.armorAbilityThreshold(),
                    EnderniumGameplayConfig.armorAbilityCooldownSeconds());
        }
    }
}
