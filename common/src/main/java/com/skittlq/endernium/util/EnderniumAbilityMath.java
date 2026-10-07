package com.skittlq.endernium.util;

public final class EnderniumAbilityMath {
    public static final float SWORD_BARRAGE_DAMAGE_MULTIPLIER = 2.0F;

    private EnderniumAbilityMath() {
    }

    public static int storedStrikes(float storedDamage, float cost, int maximum) {
        if (!(cost > 0.0F) || maximum <= 0) {
            return 0;
        }
        return Math.min(maximum, Math.max(0, (int) Math.floor(storedDamage / cost + 1.0E-6F)));
    }

    public static float accumulateSwordCharge(float storedDamage, float healthDamage,
                                               float cost, int maximum) {
        if (!(cost > 0.0F) || maximum <= 0 || !Float.isFinite(storedDamage)
                || !Float.isFinite(healthDamage)) {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(cost * maximum,
                Math.max(0.0F, storedDamage) + Math.max(0.0F, healthDamage)));
    }

    public static float chargeAfterBarrageAttempt(float storedDamage, boolean foundValidTarget) {
        return foundValidTarget ? 0.0F : Math.max(0.0F, storedDamage);
    }

    public static float swordBarrageDamage(float normalAttackDamage) {
        return Math.max(0.0F, normalAttackDamage) * SWORD_BARRAGE_DAMAGE_MULTIPLIER;
    }

    public static float actualHealthDamage(float healthBefore, float healthAfter, float fallbackDamage) {
        if (!Float.isFinite(healthBefore) || !Float.isFinite(healthAfter)) {
            return Math.max(0.0F, fallbackDamage);
        }
        return Math.max(0.0F, healthBefore - healthAfter);
    }

    public static float partialStrikeProgress(float storedDamage, float cost, int maximum) {
        int strikes = storedStrikes(storedDamage, cost, maximum);
        if (strikes >= maximum) {
            return 1.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, (storedDamage - strikes * cost) / cost));
    }

    public static float armorStrength(float storedDamage, float capacity) {
        if (!(capacity > 0.0F) || !Float.isFinite(storedDamage)) {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, storedDamage / capacity));
    }

    public static float accumulateArmorCharge(float storedDamage, float damage, float capacity) {
        if (!(capacity > 0.0F) || !Float.isFinite(storedDamage) || !Float.isFinite(damage)) {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(capacity,
                Math.max(0.0F, storedDamage) + Math.max(0.0F, damage)));
    }

    public static float spearStrainDamage(float maxHealth, long remainingTicks, int durationTicks,
                                          float maximumHealthCostFraction) {
        if (maxHealth <= 0.0F || remainingTicks <= 0L || durationTicks <= 0
                || maximumHealthCostFraction <= 0.0F) {
            return 0.0F;
        }
        float remainingFraction = Math.min(1.0F, (float) remainingTicks / durationTicks);
        return maxHealth * Math.min(1.0F, maximumHealthCostFraction) * remainingFraction;
    }
}
