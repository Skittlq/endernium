package com.skittlq.endernium.datagen;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.item.ModItems;
import com.skittlq.endernium.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput p_275343_, CompletableFuture<HolderLookup.Provider> p_275729_, CompletableFuture<TagsProvider.TagLookup<Block>> p_275322_) {
        super(p_275343_, p_275729_, p_275322_);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.Items.ENDERNIUM_REPAIRABLE)
                .add(ModItems.ENDERNIUM_INGOT.get());
        tag(ItemTags.SWORDS)
                .add(ModItems.ENDERNIUM_SWORD.get());
        tag(ItemTags.PICKAXES)
                .add(ModItems.ENDERNIUM_PICKAXE.get());
        tag(ItemTags.SHOVELS)
                .add(ModItems.ENDERNIUM_SHOVEL.get());
        tag(ItemTags.AXES)
                .add(ModItems.ENDERNIUM_AXE.get());
        tag(ItemTags.HOES)
                .add(ModItems.ENDERNIUM_HOE.get());

        this.tag(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.ENDERNIUM_HELMET.get())
                .add(ModItems.ENDERNIUM_CHESTPLATE.get())
                .add(ModItems.ENDERNIUM_LEGGINGS.get())
                .add(ModItems.ENDERNIUM_BOOTS.get());
    }
}