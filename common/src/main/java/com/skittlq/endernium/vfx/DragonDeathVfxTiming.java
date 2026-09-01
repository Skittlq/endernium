package com.skittlq.endernium.vfx;

import net.minecraft.world.phys.Vec3;

/** Shared timing rules used by both the authoritative server reaction and client presentation. */
public final class DragonDeathVfxTiming {
    public static final double WAVE_SPEED_BLOCKS_PER_TICK = 24.0;

    private DragonDeathVfxTiming() {
    }

    /**
     * Returns the first tick on which the horizontal wavefront has reached {@code position}.
     * Height is deliberately excluded: the visible pressure wave travels across The End's plane.
     */
    public static int waveArrivalTick(Vec3 origin, Vec3 position) {
        return (int)Math.ceil(horizontalDistance(origin, position) / WAVE_SPEED_BLOCKS_PER_TICK);
    }

    public static double horizontalDistance(Vec3 first, Vec3 second) {
        double dx = first.x - second.x;
        double dz = first.z - second.z;
        return Math.sqrt(dx * dx + dz * dz);
    }
}
