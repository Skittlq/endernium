package com.skittlq.endernium.datagen;

import com.skittlq.endernium.block.ModBlocks;
import com.skittlq.endernium.item.ModItems;
import com.skittlq.endernium.item.armor.ModArmorMaterial;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.resources.Identifier;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
        blockModelGenerators.createTrivialCube(ModBlocks.ENDERNIUM_BLOCK);
        blockModelGenerators.createTrivialCube(ModBlocks.ENDERNIUM_ORE);
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_INGOT, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_SHARD, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_DUST, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_UPGRADE_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);

        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerators.generateSpear(ModItems.ENDERNIUM_SPEAR);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_HOE, ModelTemplates.FLAT_HANDHELD_ITEM);

        itemModelGenerators.generateTrimmableItem(ModItems.ENDERNIUM_HELMET,
            ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL.assetId(),
            Identifier.fromNamespaceAndPath("minecraft", "trims/items/helmet_trim"), false);
        itemModelGenerators.generateTrimmableItem(ModItems.ENDERNIUM_CHESTPLATE,
            ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL.assetId(),
            Identifier.fromNamespaceAndPath("minecraft", "trims/items/chestplate_trim"), false);
        itemModelGenerators.generateTrimmableItem(ModItems.ENDERNIUM_LEGGINGS,
            ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL.assetId(),
            Identifier.fromNamespaceAndPath("minecraft", "trims/items/leggings_trim"), false);
        itemModelGenerators.generateTrimmableItem(ModItems.ENDERNIUM_BOOTS,
            ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL.assetId(),
            Identifier.fromNamespaceAndPath("minecraft", "trims/items/boots_trim"), false);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_HORSE_ARMOR, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_NAUTILUS_ARMOR, ModelTemplates.FLAT_ITEM);
    }
}
