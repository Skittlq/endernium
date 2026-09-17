package com.skittlq.endernium.worldgen;

import com.mojang.serialization.MapCodec;
import com.skittlq.endernium.Endernium;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class ModFeatures {
    public static final MapCodec<SurfaceEnderniumOreFeature> SURFACE_ENDERNIUM_ORE = registerSurfaceEnderniumOre();

    private ModFeatures() {
    }

    private static MapCodec<SurfaceEnderniumOreFeature> registerSurfaceEnderniumOre() {
        return Registry.register(
                BuiltInRegistries.FEATURE_TYPE,
                Identifier.fromNamespaceAndPath(Endernium.MOD_ID, EnderniumFeatures.SURFACE_ENDERNIUM_ORE_ID),
                SurfaceEnderniumOreFeature.CODEC
        );
    }

    public static void register() {
    }
}
