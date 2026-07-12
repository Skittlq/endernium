package com.skittlq.endernium.datagen;

import com.skittlq.endernium.block.ModBlocks;
import com.skittlq.endernium.util.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public ModBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        builder(BlockTags.BEACON_BASE_BLOCKS)
                .add(blockKey(ModBlocks.ENDERNIUM_BLOCK));

        builder(ModTags.Blocks.NEEDS_ENDERNIUM_TOOL);
        builder(ModTags.Blocks.INCORRECT_FOR_ENDERNIUM_TOOL);
    }

    private static ResourceKey<Block> blockKey(Block block) {
        return BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow();
    }
}

