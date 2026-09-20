package com.skittlq.endernium.datagen;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.block.ModBlocks;
import com.skittlq.endernium.item.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;

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
        itemModels.generateSpear(ModItems.ENDERNIUM_SPEAR.get());

        /* BLOCKS */
        blockModels.createTrivialCube(ModBlocks.ENDERNIUM_BLOCK.get());
        blockModels.createTrivialCube(ModBlocks.ENDERNIUM_ORE.get());

        itemModels.generateDynamicTrimmableItem(ModItems.ENDERNIUM_HELMET.get(), ItemModelGenerators.TRIM_PREFIX_HELMET);
        itemModels.generateDynamicTrimmableItem(ModItems.ENDERNIUM_CHESTPLATE.get(), ItemModelGenerators.TRIM_PREFIX_CHESTPLATE);
        itemModels.generateDynamicTrimmableItem(ModItems.ENDERNIUM_LEGGINGS.get(), ItemModelGenerators.TRIM_PREFIX_LEGGINGS);
        itemModels.generateDynamicTrimmableItem(ModItems.ENDERNIUM_BOOTS.get(), ItemModelGenerators.TRIM_PREFIX_BOOTS);
        itemModels.generateFlatItem(ModItems.ENDERNIUM_HORSE_ARMOR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.ENDERNIUM_NAUTILUS_ARMOR.get(), ModelTemplates.FLAT_ITEM);
    }

}
