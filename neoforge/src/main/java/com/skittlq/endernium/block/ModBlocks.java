package com.skittlq.endernium.block;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Endernium.MODID);

    public static final DeferredBlock<Block> ENDERNIUM_BLOCK = registerBlock(EnderniumBlocks.ENDERNIUM_BLOCK);
    public static final DeferredBlock<Block> ENDERNIUM_ORE = registerBlock(EnderniumBlocks.ENDERNIUM_ORE);

    private static DeferredBlock<Block> registerBlock(EnderniumBlocks definition) {
        DeferredBlock<Block> block = BLOCKS.registerBlock(definition.id(), definition::create);
        definition.bindBlock(block);
        registerBlockItem(definition, block);
        return block;
    }

    private static void registerBlockItem(EnderniumBlocks definition, DeferredBlock<Block> block) {
        DeferredItem<BlockItem> item = ModItems.ITEMS.registerItem(
                definition.id(),
                properties -> new BlockItem(block.get(), properties.useBlockDescriptionPrefix())
        );
        definition.bindItem(item);
    }

    public static Block enderniumOreBlock() {
        return EnderniumBlocks.ENDERNIUM_ORE.block();
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}