package com.skittlq.endernium.util;

public final class EnderniumCooldowns {
    private EnderniumCooldowns() {
    }

    public static long deadline(long currentTick, long durationTicks) {
        if (durationTicks <= 0L) {
            return currentTick;
        }
        return currentTick > Long.MAX_VALUE - durationTicks
                ? Long.MAX_VALUE : currentTick + durationTicks;
    }

    public static boolean isDeadlineActive(long currentTick, long endTick, long durationTicks) {
        if (durationTicks <= 0L || endTick <= currentTick) {
            return false;
        }
        long earliestValidTick = endTick < Long.MIN_VALUE + durationTicks
                ? Long.MIN_VALUE : endTick - durationTicks;
        return currentTick >= earliestValidTick;
    }

    public static boolean isElapsedCooldownActive(long currentTick, long lastUsedTick, long durationTicks) {
        return durationTicks > 0L
                && lastUsedTick > 0L
                && currentTick >= lastUsedTick
                && currentTick - lastUsedTick < durationTicks;
    }
}
