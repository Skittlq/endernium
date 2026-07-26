package com.skittlq.endernium.worldgen;

import com.skittlq.endernium.Endernium;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public final class ModFeatures {
    public static final Feature<OreConfiguration> SURFACE_ENDERNIUM_ORE = registerSurfaceEnderniumOre();

    private ModFeatures() {
    }

    private static Feature<OreConfiguration> registerSurfaceEnderniumOre() {
        Feature<OreConfiguration> feature = Registry.register(
                BuiltInRegistries.FEATURE,
                Identifier.fromNamespaceAndPath(Endernium.MOD_ID, EnderniumFeatures.SURFACE_ENDERNIUM_ORE_ID),
                new SurfaceEnderniumOreFeature(OreConfiguration.CODEC)
        );
        EnderniumFeatures.bindSurfaceEnderniumOre(() -> feature);
        return feature;
    }

    public static void register() {
    }
}