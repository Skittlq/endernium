package com.skittlq.endernium.datagen;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.block.ModBlocks;
import com.skittlq.endernium.item.ModItems;
import com.skittlq.endernium.item.armor.ModArmorMaterial;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class ModModelProvider extends ModelProvider {
    public ModModelProvider(PackOutput output) {
        super(output, Endernium.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(ModItems.ENDERNIUM_DUST.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.ENDERNIUM_SHARD.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.ENDERNIUM_INGOT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.ENDERNIUM_UPGRADE_SMITHING_TEMPLATE.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(ModItems.ENDERNIUM_AXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.ENDERNIUM_PICKAXE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.ENDERNIUM_SHOVEL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.ENDERNIUM_SWORD.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModels.generateFlatItem(ModItems.ENDERNIUM_HOE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);

        /* BLOCKS */
        blockModels.createTrivialCube(ModBlocks.ENDERNIUM_BLOCK.get());
        blockModels.createTrivialCube(ModBlocks.ENDERNIUM_ORE.get());

        itemModels.generateTrimmableItem(ModItems.ENDERNIUM_HELMET.get(), ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL.assetId(), ResourceLocation.fromNamespaceAndPath("minecraft", "trims/items/helmet_trim"), false);
        itemModels.generateTrimmableItem(ModItems.ENDERNIUM_CHESTPLATE.get(), ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL.assetId(), ResourceLocation.fromNamespaceAndPath("minecraft", "trims/items/chestplate_trim"), false);
        itemModels.generateTrimmableItem(ModItems.ENDERNIUM_LEGGINGS.get(), ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL.assetId(), ResourceLocation.fromNamespaceAndPath("minecraft", "trims/items/leggings_trim"), false);
        itemModels.generateTrimmableItem(ModItems.ENDERNIUM_BOOTS.get(), ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL.assetId(), ResourceLocation.fromNamespaceAndPath("minecraft", "trims/items/boots_trim"), false);
    }

}
