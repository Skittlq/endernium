package com.skittlq.endernium.client;

// Client-local mirror of server-authoritative ability cooldowns, updated via network sync instead of vanilla ItemCooldowns.
public final class EnderniumClientCooldowns {
    private static long armorCooldownEndTick;
    private static int armorCooldownDurationTicks;
    private static long swordCooldownEndTick;
    private static int swordCooldownDurationTicks;

    private EnderniumClientCooldowns() {
    }

    public static void setArmorCooldown(long endGameTime, int durationTicks) {
        armorCooldownEndTick = endGameTime;
        armorCooldownDurationTicks = durationTicks;
    }

    public static void setSwordCooldown(long endGameTime, int durationTicks) {
        swordCooldownEndTick = endGameTime;
        swordCooldownDurationTicks = durationTicks;
    }

    public static float armorCooldownRemainingFraction(long currentGameTime) {
        return remainingFraction(armorCooldownEndTick, armorCooldownDurationTicks, currentGameTime);
    }

    public static float swordCooldownRemainingFraction(long currentGameTime) {
        return remainingFraction(swordCooldownEndTick, swordCooldownDurationTicks, currentGameTime);
    }

    public static boolean isSwordOnCooldown(long currentGameTime) {
        return swordCooldownEndTick > currentGameTime;
    }

    private static float remainingFraction(long endTick, int durationTicks, long currentGameTime) {
        if (durationTicks <= 0 || endTick <= currentGameTime) {
            return 0.0F;
        }
        long remainingTicks = endTick - currentGameTime;
        return Math.min(1.0F, (float) remainingTicks / durationTicks);
    }
}
