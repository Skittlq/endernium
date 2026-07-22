package com.skittlq.endernium.worldgen;

import com.skittlq.endernium.Endernium;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;

public final class ModWorldgen {
    private static boolean registered;

    private ModWorldgen() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_END),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                ModPlacedFeatures.ENDERNIUM_ORE_PLACED_KEY
        );
        Endernium.LOGGER.info("Registered Endernium worldgen biome modifications");
    }
}
