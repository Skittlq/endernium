package com.skittlq.endernium.config;

import com.skittlq.endernium.client.vfx.EnderniumVfxRenderMode;

public class EnderniumConfig {
    public boolean enderniumArmorAbility = true;
    public int enderniumArmorAbilityThreshold = EnderniumGameplayConfig.DEFAULT_ARMOR_ABILITY_THRESHOLD;
    public double enderniumArmorMaxStoredDamage = EnderniumGameplayConfig.DEFAULT_ARMOR_MAX_STORED_DAMAGE;
    public boolean enderniumSwordAbility = true;
    public double enderniumSwordDamagePerStrikeMultiplier = EnderniumGameplayConfig.DEFAULT_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER;
    public int enderniumSwordMaxStrikes = EnderniumGameplayConfig.DEFAULT_SWORD_MAX_STRIKES;
    public int enderniumSpearStrainDurationSeconds = EnderniumGameplayConfig.DEFAULT_SPEAR_STRAIN_DURATION_SECONDS;
    public double enderniumSpearMaximumHealthCostPercent = EnderniumGameplayConfig.DEFAULT_SPEAR_MAXIMUM_HEALTH_COST_PERCENT;
    public boolean enderniumToolsVeinMining = true;
    public EnderniumVfxRenderMode vfxRenderMode = EnderniumVfxRenderMode.AUTO;

    public EnderniumConfig copy() {
        EnderniumConfig copy = new EnderniumConfig();
        copy.enderniumArmorAbility = enderniumArmorAbility;
        copy.enderniumArmorAbilityThreshold = enderniumArmorAbilityThreshold;
        copy.enderniumArmorMaxStoredDamage = enderniumArmorMaxStoredDamage;
        copy.enderniumSwordAbility = enderniumSwordAbility;
        copy.enderniumSwordDamagePerStrikeMultiplier = enderniumSwordDamagePerStrikeMultiplier;
        copy.enderniumSwordMaxStrikes = enderniumSwordMaxStrikes;
        copy.enderniumSpearStrainDurationSeconds = enderniumSpearStrainDurationSeconds;
        copy.enderniumSpearMaximumHealthCostPercent = enderniumSpearMaximumHealthCostPercent;
        copy.enderniumToolsVeinMining = enderniumToolsVeinMining;
        copy.vfxRenderMode = vfxRenderMode;
        return copy;
    }

    public static EnderniumConfig sanitize(EnderniumConfig rawConfig) {
        EnderniumConfig sanitized = rawConfig == null ? new EnderniumConfig() : rawConfig.copy();
        sanitized.enderniumArmorAbilityThreshold = Math.max(1,
                Math.min(2048, sanitized.enderniumArmorAbilityThreshold));
        sanitized.enderniumArmorMaxStoredDamage = finiteClamp(sanitized.enderniumArmorMaxStoredDamage,
                1.0D, EnderniumGameplayConfig.MAX_ARMOR_STORED_DAMAGE,
                EnderniumGameplayConfig.DEFAULT_ARMOR_MAX_STORED_DAMAGE);
        sanitized.enderniumSwordDamagePerStrikeMultiplier = finiteClamp(
                sanitized.enderniumSwordDamagePerStrikeMultiplier,
                EnderniumGameplayConfig.MIN_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER,
                EnderniumGameplayConfig.MAX_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER,
                EnderniumGameplayConfig.DEFAULT_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER);
        sanitized.enderniumSwordMaxStrikes = Math.max(1,
                Math.min(EnderniumGameplayConfig.MAX_SWORD_STRIKES, sanitized.enderniumSwordMaxStrikes));
        sanitized.enderniumSpearStrainDurationSeconds = Math.max(1,
                Math.min(EnderniumGameplayConfig.MAX_SPEAR_STRAIN_DURATION_SECONDS,
                        sanitized.enderniumSpearStrainDurationSeconds));
        sanitized.enderniumSpearMaximumHealthCostPercent = finiteClamp(
                sanitized.enderniumSpearMaximumHealthCostPercent, 0.0D, 100.0D,
                EnderniumGameplayConfig.DEFAULT_SPEAR_MAXIMUM_HEALTH_COST_PERCENT);
        if (sanitized.vfxRenderMode == null) {
            sanitized.vfxRenderMode = EnderniumVfxRenderMode.AUTO;
        }
        return sanitized;
    }

    private static double finiteClamp(double value, double minimum, double maximum, double fallback) {
        return Double.isFinite(value) ? Math.max(minimum, Math.min(maximum, value)) : fallback;
    }
}
