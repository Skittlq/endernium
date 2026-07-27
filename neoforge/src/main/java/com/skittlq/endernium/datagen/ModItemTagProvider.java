package com.skittlq.endernium.datagen;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.item.ModItems;
import com.skittlq.endernium.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Endernium.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.Items.ENDERNIUM_REPAIRABLE)
                .add(ModItems.ENDERNIUM_INGOT.getKey());
        tag(ModTags.Items.INGOTS_ENDERNIUM)
                .add(ModItems.ENDERNIUM_INGOT.getKey());
        tag(ItemTags.BEACON_PAYMENT_ITEMS)
                .add(ModItems.ENDERNIUM_INGOT.getKey());

        tag(ItemTags.SWORDS)
                .add(ModItems.ENDERNIUM_SWORD.getKey());
        tag(ItemTags.SPEARS)
                .add(ModItems.ENDERNIUM_SPEAR.getKey());
        tag(ItemTags.PICKAXES)
                .add(ModItems.ENDERNIUM_PICKAXE.getKey());
        tag(ItemTags.SHOVELS)
                .add(ModItems.ENDERNIUM_SHOVEL.getKey());
        tag(ItemTags.AXES)
                .add(ModItems.ENDERNIUM_AXE.getKey());
        tag(ItemTags.HOES)
                .add(ModItems.ENDERNIUM_HOE.getKey());

        tag(ItemTags.ARMOR_ENCHANTABLE)
                .add(ModItems.ENDERNIUM_HELMET.getKey())
                .add(ModItems.ENDERNIUM_CHESTPLATE.getKey())
                .add(ModItems.ENDERNIUM_LEGGINGS.getKey())
                .add(ModItems.ENDERNIUM_BOOTS.getKey());
        tag(ItemTags.HEAD_ARMOR)
                .add(ModItems.ENDERNIUM_HELMET.getKey());
        tag(ItemTags.CHEST_ARMOR)
                .add(ModItems.ENDERNIUM_CHESTPLATE.getKey());
        tag(ItemTags.LEG_ARMOR)
                .add(ModItems.ENDERNIUM_LEGGINGS.getKey());
        tag(ItemTags.FOOT_ARMOR)
                .add(ModItems.ENDERNIUM_BOOTS.getKey());
        tag(ItemTags.HEAD_ARMOR_ENCHANTABLE)
                .add(ModItems.ENDERNIUM_HELMET.getKey());
        tag(ItemTags.CHEST_ARMOR_ENCHANTABLE)
                .add(ModItems.ENDERNIUM_CHESTPLATE.getKey());
        tag(ItemTags.LEG_ARMOR_ENCHANTABLE)
                .add(ModItems.ENDERNIUM_LEGGINGS.getKey());
        tag(ItemTags.FOOT_ARMOR_ENCHANTABLE)
                .add(ModItems.ENDERNIUM_BOOTS.getKey());

    }
}
