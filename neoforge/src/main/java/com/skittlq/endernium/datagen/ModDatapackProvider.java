package com.skittlq.endernium.datagen;

import com.skittlq.endernium.trim.ModTrimMaterials;
import com.skittlq.endernium.worldgen.ModBiomeModifiers;
import com.skittlq.endernium.worldgen.ModConfiguredFeatures;
import com.skittlq.endernium.worldgen.ModPlacedFeatures;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModDatapackProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.FEATURE, ModConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap)
            .add(Registries.TRIM_MATERIAL, ModTrimMaterials::bootstrap);

    private ModDatapackProvider() {
    }
}
