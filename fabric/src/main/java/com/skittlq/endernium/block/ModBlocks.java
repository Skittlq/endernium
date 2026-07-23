package com.skittlq.endernium.block;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.item.ModItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public final class ModBlocks {
    public static final Block ENDERNIUM_BLOCK = registerBlock(EnderniumBlocks.ENDERNIUM_BLOCK);
    public static final Block ENDERNIUM_ORE = registerBlock(EnderniumBlocks.ENDERNIUM_ORE);

    public static final Item ENDERNIUM_BLOCK_ITEM = registerBlockItem(EnderniumBlocks.ENDERNIUM_BLOCK, ENDERNIUM_BLOCK);
    public static final Item ENDERNIUM_ORE_ITEM = registerBlockItem(EnderniumBlocks.ENDERNIUM_ORE, ENDERNIUM_ORE);

    private ModBlocks() {
    }

    private static Block registerBlock(EnderniumBlocks definition) {
        Block block = registerBlock(definition.id(), definition::create);
        definition.bindBlock(() -> block);
        return block;
    }

    private static <T extends Block> T registerBlock(String name, Function<BlockBehaviour.Properties, T> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(Endernium.MOD_ID, name);
        return Registry.register(BuiltInRegistries.BLOCK, id,
                factory.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id))));
    }

    private static Item registerBlockItem(EnderniumBlocks definition, Block block) {
        Item item = ModItems.registerItem(definition.id(), properties -> new BlockItem(block, properties.useBlockDescriptionPrefix()));
        definition.bindItem(() -> item);
        return item;
    }

    public static Block enderniumOreBlock() {
        return EnderniumBlocks.ENDERNIUM_ORE.block();
    }

    public static void register() {
        Endernium.LOGGER.info("Registering Mod Blocks for {}", Endernium.MOD_ID);
    }
}