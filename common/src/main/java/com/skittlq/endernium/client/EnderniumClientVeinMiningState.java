package com.skittlq.endernium.client;

public final class EnderniumClientVeinMiningState {
    private static boolean active;
    private static long blockEndGameTime;
    private static int blockDurationTicks;

    private EnderniumClientVeinMiningState() {
    }

    public static boolean isActive() {
        return active;
    }

    public static float blockProgress(long gameTime) {
        if (!active || blockDurationTicks <= 0) {
            return 0.0F;
        }
        long remainingTicks = Math.max(0L, blockEndGameTime - gameTime);
        float remainingFraction = Math.min(1.0F, (float) remainingTicks / blockDurationTicks);
        return 1.0F - remainingFraction;
    }

    public static void setState(boolean active, long blockEndGameTime, int blockDurationTicks) {
        EnderniumClientVeinMiningState.active = active;
        EnderniumClientVeinMiningState.blockEndGameTime = blockEndGameTime;
        EnderniumClientVeinMiningState.blockDurationTicks = Math.max(0, blockDurationTicks);
    }

    public static void reset() {
        active = false;
        blockEndGameTime = 0L;
        blockDurationTicks = 0;
    }
}
