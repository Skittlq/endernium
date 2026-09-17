package com.skittlq.endernium.worldgen;

import com.skittlq.endernium.block.EnderniumBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import java.util.List;

public final class ModConfiguredFeatures {
    public static final ResourceKey<Feature> ENDERNIUM_ORE_KEY = registerKey("endernium_ore");

    private ModConfiguredFeatures() {
    }

    public static void bootstrap(BootstrapContext<Feature> context) {
        RuleTest endReplaceables = new BlockMatchTest(Blocks.END_STONE);

        context.register(ENDERNIUM_ORE_KEY, new SurfaceEnderniumOreFeature(
                List.of(BlockReplacement.replace(endReplaceables, EnderniumBlocks.ENDERNIUM_ORE.block().defaultBlockState())),
                4,
                0.0F
        ));
    }

    public static ResourceKey<Feature> registerKey(String name) {
        return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath("endernium", name));
    }
}
