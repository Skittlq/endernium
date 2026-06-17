package com.skittlq.endernium.datagen;

import com.skittlq.endernium.util.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public ModBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        valueLookupBuilder(ModTags.Blocks.NEEDS_ENDERNIUM_TOOL);
        valueLookupBuilder(ModTags.Blocks.INCORRECT_FOR_ENDERNIUM_TOOL);
    }
}

