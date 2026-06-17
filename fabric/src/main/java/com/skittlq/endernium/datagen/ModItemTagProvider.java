package com.skittlq.endernium.datagen;

import com.skittlq.endernium.item.ModItems;
import com.skittlq.endernium.util.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagsProvider.ItemTagsProvider {
    public ModItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture,
                              ModBlockTagProvider blockTagProvider) {
        super(output, completableFuture, blockTagProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        valueLookupBuilder(ModTags.Items.TRANSFORMABLE_ITEMS);

        valueLookupBuilder(ModTags.Items.ENDERNIUM_REPAIRABLE)
                .add(ModItems.ENDERNIUM_INGOT);
        valueLookupBuilder(ModTags.Items.INGOTS_ENDERNIUM)
                .add(ModItems.ENDERNIUM_INGOT);

        valueLookupBuilder(ItemTags.SWORDS)
                .add(ModItems.ENDERNIUM_SWORD);
        valueLookupBuilder(ItemTags.PICKAXES)
                .add(ModItems.ENDERNIUM_PICKAXE);
        valueLookupBuilder(ItemTags.SHOVELS)
                .add(ModItems.ENDERNIUM_SHOVEL);
        valueLookupBuilder(ItemTags.AXES)
                .add(ModItems.ENDERNIUM_AXE);
        valueLookupBuilder(ItemTags.HOES)
                .add(ModItems.ENDERNIUM_HOE);

        valueLookupBuilder(ItemTags.ARMOR_ENCHANTABLE)
                .add(ModItems.ENDERNIUM_HELMET)
                .add(ModItems.ENDERNIUM_CHESTPLATE)
                .add(ModItems.ENDERNIUM_LEGGINGS)
                .add(ModItems.ENDERNIUM_BOOTS);
        valueLookupBuilder(ItemTags.HEAD_ARMOR)
                .add(ModItems.ENDERNIUM_HELMET);
        valueLookupBuilder(ItemTags.CHEST_ARMOR)
                .add(ModItems.ENDERNIUM_CHESTPLATE);
        valueLookupBuilder(ItemTags.LEG_ARMOR)
                .add(ModItems.ENDERNIUM_LEGGINGS);
        valueLookupBuilder(ItemTags.FOOT_ARMOR)
                .add(ModItems.ENDERNIUM_BOOTS);
        valueLookupBuilder(ItemTags.HEAD_ARMOR_ENCHANTABLE)
                .add(ModItems.ENDERNIUM_HELMET);
        valueLookupBuilder(ItemTags.CHEST_ARMOR_ENCHANTABLE)
                .add(ModItems.ENDERNIUM_CHESTPLATE);
        valueLookupBuilder(ItemTags.LEG_ARMOR_ENCHANTABLE)
                .add(ModItems.ENDERNIUM_LEGGINGS);
        valueLookupBuilder(ItemTags.FOOT_ARMOR_ENCHANTABLE)
                .add(ModItems.ENDERNIUM_BOOTS);
        valueLookupBuilder(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.ENDERNIUM_HELMET)
                .add(ModItems.ENDERNIUM_CHESTPLATE)
                .add(ModItems.ENDERNIUM_LEGGINGS)
                .add(ModItems.ENDERNIUM_BOOTS);
        valueLookupBuilder(ItemTags.TRIM_MATERIALS)
                .add(ModItems.ENDERNIUM_INGOT);
    }
}

