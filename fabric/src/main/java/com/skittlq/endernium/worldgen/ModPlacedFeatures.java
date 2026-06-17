package com.skittlq.endernium.worldgen;

import com.skittlq.endernium.Endernium;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> ENDERNIUM_ORE_PLACED_KEY = ResourceKey.create(
            Registries.PLACED_FEATURE,
            Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "endernium_ore_placed")
    );

    private ModPlacedFeatures() {
    }
}
