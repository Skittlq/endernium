package com.skittlq.endernium.client;

// Client-local mirror of server-authoritative ability cooldowns, updated via network sync instead of vanilla ItemCooldowns.
public final class EnderniumClientCooldowns {
    private static long horseCooldownEndTick;
    private static int horseCooldownDurationTicks;
    private static long spearCooldownEndTick;
    private static int spearCooldownDurationTicks;
    private static long nautilusCooldownEndTick;
    private static int nautilusCooldownDurationTicks;

    private EnderniumClientCooldowns() {
    }

    public static void setHorseCooldown(long endGameTime, int durationTicks) {
        horseCooldownEndTick = Math.max(0L, endGameTime);
        horseCooldownDurationTicks = Math.max(0, durationTicks);
    }

    public static void setSpearStrain(long endGameTime, int durationTicks) {
        spearCooldownEndTick = Math.max(0L, endGameTime);
        spearCooldownDurationTicks = Math.max(0, durationTicks);
    }

    public static void setNautilusCooldown(long endGameTime, int durationTicks) {
        nautilusCooldownEndTick = Math.max(0L, endGameTime);
        nautilusCooldownDurationTicks = Math.max(0, durationTicks);
    }

    public static float horseCooldownRemainingFraction(long currentGameTime) {
        return remainingFraction(horseCooldownEndTick, horseCooldownDurationTicks, currentGameTime);
    }

    public static float spearCooldownRemainingFraction(long currentGameTime) {
        return remainingFraction(spearCooldownEndTick, spearCooldownDurationTicks, currentGameTime);
    }

    public static float nautilusCooldownRemainingFraction(long currentGameTime) {
        return remainingFraction(nautilusCooldownEndTick, nautilusCooldownDurationTicks, currentGameTime);
    }

    public static boolean isHorseOnCooldown(long currentGameTime) {
        return horseCooldownEndTick > currentGameTime;
    }

    public static boolean isNautilusOnCooldown(long currentGameTime) {
        return nautilusCooldownEndTick > currentGameTime;
    }

    public static void clear() {
        horseCooldownEndTick = 0L;
        horseCooldownDurationTicks = 0;
        spearCooldownEndTick = 0L;
        spearCooldownDurationTicks = 0;
        nautilusCooldownEndTick = 0L;
        nautilusCooldownDurationTicks = 0;
    }

    private static float remainingFraction(long endTick, int durationTicks, long currentGameTime) {
        if (durationTicks <= 0 || endTick <= currentGameTime) {
            return 0.0F;
        }
        long remainingTicks = endTick - currentGameTime;
        return Math.min(1.0F, (float) remainingTicks / durationTicks);
    }
}
