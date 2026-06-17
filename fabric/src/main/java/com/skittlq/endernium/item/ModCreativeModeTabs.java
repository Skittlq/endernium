package com.skittlq.endernium.item;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.block.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTabs {
    public static final CreativeModeTab ENDERNIUM_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "endernium"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(ModItems.ENDERNIUM_INGOT))
                    .title(Component.translatable("creativetab.endernium"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ENDERNIUM_INGOT);
                        output.accept(ModItems.ENDERNIUM_SHARD);
                        output.accept(ModItems.ENDERNIUM_DUST);
                        output.accept(ModItems.ENDERNIUM_UPGRADE_SMITHING_TEMPLATE);
                        output.accept(ModBlocks.ENDERNIUM_BLOCK_ITEM);
                        output.accept(ModBlocks.ENDERNIUM_ORE_ITEM);
                        output.accept(ModItems.ENDERNIUM_SWORD);
                        output.accept(ModItems.ENDERNIUM_SHOVEL);
                        output.accept(ModItems.ENDERNIUM_PICKAXE);
                        output.accept(ModItems.ENDERNIUM_AXE);
                        output.accept(ModItems.ENDERNIUM_HOE);
                        output.accept(ModItems.ENDERNIUM_HELMET);
                        output.accept(ModItems.ENDERNIUM_CHESTPLATE);
                        output.accept(ModItems.ENDERNIUM_LEGGINGS);
                        output.accept(ModItems.ENDERNIUM_BOOTS);
                    }).build());

    public static void registerModCreativeModeTabs() {
        Endernium.LOGGER.info("Registering Creative Mode Tabs for " + Endernium.MOD_ID);
    }
}