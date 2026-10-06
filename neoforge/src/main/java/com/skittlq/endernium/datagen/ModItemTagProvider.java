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
                .add(ModItems.ENDERNIUM_INGOT.key());
        tag(ModTags.Items.INGOTS_ENDERNIUM)
                .add(ModItems.ENDERNIUM_INGOT.key());
        tag(ItemTags.BEACON_PAYMENT_ITEMS)
                .add(ModItems.ENDERNIUM_INGOT.key());

        tag(ItemTags.SWORDS)
                .add(ModItems.ENDERNIUM_SWORD.key());
        tag(ItemTags.SPEARS)
                .add(ModItems.ENDERNIUM_SPEAR.key());
        tag(ItemTags.PICKAXES)
                .add(ModItems.ENDERNIUM_PICKAXE.key());
        tag(ItemTags.SHOVELS)
                .add(ModItems.ENDERNIUM_SHOVEL.key());
        tag(ItemTags.AXES)
                .add(ModItems.ENDERNIUM_AXE.key());
        tag(ItemTags.HOES)
                .add(ModItems.ENDERNIUM_HOE.key());

        tag(ItemTags.ARMOR_ENCHANTABLE)
                .add(ModItems.ENDERNIUM_HELMET.key())
                .add(ModItems.ENDERNIUM_CHESTPLATE.key())
                .add(ModItems.ENDERNIUM_LEGGINGS.key())
                .add(ModItems.ENDERNIUM_BOOTS.key());
        tag(ItemTags.HEAD_ARMOR)
                .add(ModItems.ENDERNIUM_HELMET.key());
        tag(ItemTags.CHEST_ARMOR)
                .add(ModItems.ENDERNIUM_CHESTPLATE.key());
        tag(ItemTags.LEG_ARMOR)
                .add(ModItems.ENDERNIUM_LEGGINGS.key());
        tag(ItemTags.FOOT_ARMOR)
                .add(ModItems.ENDERNIUM_BOOTS.key());
        tag(ItemTags.HEAD_ARMOR_ENCHANTABLE)
                .add(ModItems.ENDERNIUM_HELMET.key());
        tag(ItemTags.CHEST_ARMOR_ENCHANTABLE)
                .add(ModItems.ENDERNIUM_CHESTPLATE.key());
        tag(ItemTags.LEG_ARMOR_ENCHANTABLE)
                .add(ModItems.ENDERNIUM_LEGGINGS.key());
        tag(ItemTags.FOOT_ARMOR_ENCHANTABLE)
                .add(ModItems.ENDERNIUM_BOOTS.key());

    }
}
