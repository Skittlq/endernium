package com.skittlq.endernium.datagen;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.block.EnderniumBlocks;
import com.skittlq.endernium.trim.ModTrimMaterials;
import com.skittlq.endernium.worldgen.ModConfiguredFeatures;
import com.skittlq.endernium.worldgen.SurfaceEnderniumOreFeature;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Util;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;

import java.util.concurrent.CompletableFuture;
import java.util.List;

public class ModDynamicRegistryProvider extends FabricDynamicRegistryProvider {
    public ModDynamicRegistryProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        entries.add(
                ModConfiguredFeatures.ENDERNIUM_ORE_KEY,
                new SurfaceEnderniumOreFeature(
                        List.of(BlockReplacement.replace(
                                new BlockMatchTest(Blocks.END_STONE),
                                EnderniumBlocks.ENDERNIUM_ORE.block().defaultBlockState()
                        )),
                        4,
                        0.0F
                )
        );

        entries.add(
                ModTrimMaterials.ENDERNIUM,
                new TrimMaterial(
                        ModTrimMaterials.ENDERNIUM.identifier(),
                        Component.translatable(Util.makeDescriptionId("trim_material", ModTrimMaterials.ENDERNIUM.identifier()))
                                .withStyle(Style.EMPTY.withColor(TextColor.parseColor("#031cfc").getOrThrow()))
                )
        );
    }

    @Override
    public String getName() {
        return Endernium.MOD_ID + " Dynamic Registry Provider";
    }
}
