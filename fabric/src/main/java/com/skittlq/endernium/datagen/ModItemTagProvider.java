package com.skittlq.endernium.datagen;

import com.skittlq.endernium.item.ModItems;
import com.skittlq.endernium.util.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagsProvider.ItemTagsProvider {
    public ModItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture,
                              ModBlockTagProvider blockTagProvider) {
        super(output, completableFuture, blockTagProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        builder(ModTags.Items.TRANSFORMABLE_ITEMS);

        builder(ModTags.Items.ENDERNIUM_REPAIRABLE)
                .add(itemKey(ModItems.ENDERNIUM_INGOT));
        builder(ModTags.Items.INGOTS_ENDERNIUM)
                .add(itemKey(ModItems.ENDERNIUM_INGOT));
        builder(ItemTags.BEACON_PAYMENT_ITEMS)
                .add(itemKey(ModItems.ENDERNIUM_INGOT));

        builder(ItemTags.SWORDS)
                .add(itemKey(ModItems.ENDERNIUM_SWORD));
        builder(ItemTags.SPEARS)
                .add(itemKey(ModItems.ENDERNIUM_SPEAR));
        builder(ItemTags.PICKAXES)
                .add(itemKey(ModItems.ENDERNIUM_PICKAXE));
        builder(ItemTags.SHOVELS)
                .add(itemKey(ModItems.ENDERNIUM_SHOVEL));
        builder(ItemTags.AXES)
                .add(itemKey(ModItems.ENDERNIUM_AXE));
        builder(ItemTags.HOES)
                .add(itemKey(ModItems.ENDERNIUM_HOE));

        builder(ItemTags.ARMOR_ENCHANTABLE)
                .add(itemKey(ModItems.ENDERNIUM_HELMET))
                .add(itemKey(ModItems.ENDERNIUM_CHESTPLATE))
                .add(itemKey(ModItems.ENDERNIUM_LEGGINGS))
                .add(itemKey(ModItems.ENDERNIUM_BOOTS));
        builder(ItemTags.HEAD_ARMOR)
                .add(itemKey(ModItems.ENDERNIUM_HELMET));
        builder(ItemTags.CHEST_ARMOR)
                .add(itemKey(ModItems.ENDERNIUM_CHESTPLATE));
        builder(ItemTags.LEG_ARMOR)
                .add(itemKey(ModItems.ENDERNIUM_LEGGINGS));
        builder(ItemTags.FOOT_ARMOR)
                .add(itemKey(ModItems.ENDERNIUM_BOOTS));
        builder(ItemTags.HEAD_ARMOR_ENCHANTABLE)
                .add(itemKey(ModItems.ENDERNIUM_HELMET));
        builder(ItemTags.CHEST_ARMOR_ENCHANTABLE)
                .add(itemKey(ModItems.ENDERNIUM_CHESTPLATE));
        builder(ItemTags.LEG_ARMOR_ENCHANTABLE)
                .add(itemKey(ModItems.ENDERNIUM_LEGGINGS));
        builder(ItemTags.FOOT_ARMOR_ENCHANTABLE)
                .add(itemKey(ModItems.ENDERNIUM_BOOTS));
        builder(ItemTags.TRIMMABLE_ARMOR)
                .add(itemKey(ModItems.ENDERNIUM_HELMET))
                .add(itemKey(ModItems.ENDERNIUM_CHESTPLATE))
                .add(itemKey(ModItems.ENDERNIUM_LEGGINGS))
                .add(itemKey(ModItems.ENDERNIUM_BOOTS));
        builder(ItemTags.TRIM_MATERIALS)
                .add(itemKey(ModItems.ENDERNIUM_INGOT));
    }

    private static ResourceKey<Item> itemKey(Item item) {
        return BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow();
    }
}

