package com.skittlq.endernium.worldgen;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import java.util.Objects;
import java.util.function.Supplier;

public final class EnderniumFeatures {
    public static final String SURFACE_ENDERNIUM_ORE_ID = "surface_endernium_ore";

    private static Supplier<? extends Feature<OreConfiguration>> surfaceEnderniumOre = () -> {
        throw new IllegalStateException("surface_endernium_ore feature has not been bound to a loader registry yet");
    };

    private EnderniumFeatures() {
    }

    public static void bindSurfaceEnderniumOre(Supplier<? extends Feature<OreConfiguration>> supplier) {
        surfaceEnderniumOre = Objects.requireNonNull(supplier);
    }

    public static Feature<OreConfiguration> surfaceEnderniumOre() {
        return surfaceEnderniumOre.get();
    }
}
