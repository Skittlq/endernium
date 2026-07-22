package com.skittlq.endernium.block;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.item.ModItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public final class ModBlocks {
    public static final Block ENDERNIUM_BLOCK = registerBlock("endernium_block",
            properties -> new Block(properties.strength(55.0F).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)));

    public static final Block ENDERNIUM_ORE = registerBlock("endernium_ore",
            properties -> new DropExperienceBlock(UniformInt.of(2, 4), properties.requiresCorrectToolForDrops().strength(12.5F, 1600.0F).sound(SoundType.AMETHYST)));

    public static final Item ENDERNIUM_BLOCK_ITEM = registerBlockItem("endernium_block", ENDERNIUM_BLOCK);
    public static final Item ENDERNIUM_ORE_ITEM = registerBlockItem("endernium_ore", ENDERNIUM_ORE);

    private ModBlocks() {
    }

    private static <T extends Block> T registerBlock(String name, Function<BlockBehaviour.Properties, T> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(Endernium.MOD_ID, name);
        return Registry.register(BuiltInRegistries.BLOCK, id,
                factory.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id))));
    }

    private static Item registerBlockItem(String name, Block block) {
        return ModItems.registerItem(name, properties -> new BlockItem(block, properties.useBlockDescriptionPrefix()));
    }

    public static Block enderniumOreBlock() {
        return ENDERNIUM_ORE;
    }

    public static void register() {
        Endernium.LOGGER.info("Registering Mod Blocks for {}", Endernium.MOD_ID);
    }
}
