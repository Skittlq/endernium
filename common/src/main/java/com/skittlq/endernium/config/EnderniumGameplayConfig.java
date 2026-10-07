package com.skittlq.endernium.config;

import java.util.Objects;

public final class EnderniumGameplayConfig {
    public static final double DEFAULT_SWORD_DAMAGE_PER_STRIKE = 24.0D;
    public static final double DEFAULT_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER =
            DEFAULT_SWORD_DAMAGE_PER_STRIKE / 9.0D;
    public static final int DEFAULT_SWORD_MAX_STRIKES = 15;
    public static final int DEFAULT_ARMOR_ABILITY_THRESHOLD = 4;
    public static final double DEFAULT_ARMOR_MAX_STORED_DAMAGE = 24.0D;
    public static final int DEFAULT_SPEAR_STRAIN_DURATION_SECONDS = 8;
    public static final double DEFAULT_SPEAR_MAXIMUM_HEALTH_COST_PERCENT = 40.0D;
    public static final double MIN_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER = 0.1D;
    public static final double MAX_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER = 100.0D;
    public static final int MAX_SWORD_STRIKES = 64;
    public static final double MAX_ARMOR_STORED_DAMAGE = 2048.0D;
    public static final int MAX_SPEAR_STRAIN_DURATION_SECONDS = 3600;

    private static Settings settings = new Settings() {
        @Override public boolean swordAbilityEnabled() { return true; }
        @Override public double swordDamagePerStrikeMultiplier() { return DEFAULT_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER; }
        @Override public int swordMaxStrikes() { return DEFAULT_SWORD_MAX_STRIKES; }
        @Override public boolean toolsVeinMiningEnabled() { return true; }
        @Override public boolean armorAbilityEnabled() { return true; }
        @Override public int armorAbilityThreshold() { return DEFAULT_ARMOR_ABILITY_THRESHOLD; }
        @Override public double armorMaxStoredDamage() { return DEFAULT_ARMOR_MAX_STORED_DAMAGE; }
        @Override public int spearStrainDurationSeconds() { return DEFAULT_SPEAR_STRAIN_DURATION_SECONDS; }
        @Override public double spearMaximumHealthCostPercent() { return DEFAULT_SPEAR_MAXIMUM_HEALTH_COST_PERCENT; }
    };

    private EnderniumGameplayConfig() {
    }

    public static void bind(Settings newSettings) { settings = Objects.requireNonNull(newSettings); }
    public static boolean swordAbilityEnabled() { return settings.swordAbilityEnabled(); }
    public static double swordDamagePerStrikeMultiplier() {
        return clamp(settings.swordDamagePerStrikeMultiplier(),
                MIN_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER, MAX_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER);
    }
    public static int swordMaxStrikes() {
        return Math.max(1, Math.min(MAX_SWORD_STRIKES, settings.swordMaxStrikes()));
    }
    public static boolean toolsVeinMiningEnabled() { return settings.toolsVeinMiningEnabled(); }
    public static boolean armorAbilityEnabled() { return settings.armorAbilityEnabled(); }
    public static int armorAbilityThreshold() {
        return Math.max(1, Math.min(2048, settings.armorAbilityThreshold()));
    }
    public static double armorMaxStoredDamage() {
        return clamp(settings.armorMaxStoredDamage(), 1.0D, MAX_ARMOR_STORED_DAMAGE);
    }
    public static int spearStrainDurationSeconds() {
        return Math.max(1, Math.min(MAX_SPEAR_STRAIN_DURATION_SECONDS,
                settings.spearStrainDurationSeconds()));
    }
    public static double spearMaximumHealthCostPercent() {
        return clamp(settings.spearMaximumHealthCostPercent(), 0.0D, 100.0D);
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Double.isFinite(value) ? Math.max(minimum, Math.min(maximum, value)) : minimum;
    }

    public interface Settings {
        boolean swordAbilityEnabled();
        double swordDamagePerStrikeMultiplier();
        int swordMaxStrikes();
        boolean toolsVeinMiningEnabled();
        boolean armorAbilityEnabled();
        int armorAbilityThreshold();
        double armorMaxStoredDamage();
        int spearStrainDurationSeconds();
        double spearMaximumHealthCostPercent();
    }

    public record Snapshot(boolean swordAbilityEnabled,
                           double swordDamagePerStrikeMultiplier,
                           int swordMaxStrikes,
                           boolean toolsVeinMiningEnabled,
                           boolean armorAbilityEnabled,
                           int armorThreshold,
                           double armorMaxStoredDamage,
                           int spearStrainDurationSeconds,
                           double spearMaximumHealthCostPercent) {
        public static Snapshot defaults() {
            return new Snapshot(true, DEFAULT_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER,
                    DEFAULT_SWORD_MAX_STRIKES, true, true, DEFAULT_ARMOR_ABILITY_THRESHOLD,
                    DEFAULT_ARMOR_MAX_STORED_DAMAGE, DEFAULT_SPEAR_STRAIN_DURATION_SECONDS,
                    DEFAULT_SPEAR_MAXIMUM_HEALTH_COST_PERCENT);
        }

        public static Snapshot current() {
            return new Snapshot(EnderniumGameplayConfig.swordAbilityEnabled(),
                    EnderniumGameplayConfig.swordDamagePerStrikeMultiplier(),
                    EnderniumGameplayConfig.swordMaxStrikes(),
                    EnderniumGameplayConfig.toolsVeinMiningEnabled(),
                    EnderniumGameplayConfig.armorAbilityEnabled(),
                    EnderniumGameplayConfig.armorAbilityThreshold(),
                    EnderniumGameplayConfig.armorMaxStoredDamage(),
                    EnderniumGameplayConfig.spearStrainDurationSeconds(),
                    EnderniumGameplayConfig.spearMaximumHealthCostPercent());
        }
    }
}
