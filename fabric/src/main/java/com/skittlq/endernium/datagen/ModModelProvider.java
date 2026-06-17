package com.skittlq.endernium.datagen;

import com.skittlq.endernium.item.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {

    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_INGOT, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_SHARD, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_DUST, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_UPGRADE_SMITHING_TEMPLATE, ModelTemplates.FLAT_ITEM);

        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_AXE, ModelTemplates.FLAT_HANDHELD_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_HOE, ModelTemplates.FLAT_HANDHELD_ITEM);

        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_HELMET, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_CHESTPLATE, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_LEGGINGS, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.ENDERNIUM_BOOTS, ModelTemplates.FLAT_ITEM);
    }
}
