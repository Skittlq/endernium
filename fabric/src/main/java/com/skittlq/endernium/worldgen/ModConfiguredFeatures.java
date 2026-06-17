package com.skittlq.endernium.worldgen;

import com.skittlq.endernium.Endernium;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class ModConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ENDERNIUM_ORE_KEY = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "endernium_ore")
    );

    private ModConfiguredFeatures() {
    }
}
