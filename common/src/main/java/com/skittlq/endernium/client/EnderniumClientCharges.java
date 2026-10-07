package com.skittlq.endernium.client;

public final class EnderniumClientCharges {
    private static float swordStoredDamage;
    private static float armorStoredDamage;

    private EnderniumClientCharges() {
    }

    public static float swordStoredDamage() { return swordStoredDamage; }
    public static float armorStoredDamage() { return armorStoredDamage; }
    public static void setSwordStoredDamage(float value) { swordStoredDamage = finiteNonNegative(value); }
    public static void setArmorStoredDamage(float value) { armorStoredDamage = finiteNonNegative(value); }

    public static void clear() {
        swordStoredDamage = 0.0F;
        armorStoredDamage = 0.0F;
    }

    private static float finiteNonNegative(float value) {
        return Float.isFinite(value) ? Math.max(0.0F, value) : 0.0F;
    }
}
