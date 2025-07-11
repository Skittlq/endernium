package com.skittlq.endernium.block;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.item.ModItems;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Endernium.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Endernium.MODID);

    public static final RegistryObject<Block> ENDERNIUM_BLOCK = BLOCKS.register("endernium_block",
            () -> new Block(BlockBehaviour.Properties.of().strength(8f, 1200.0F).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)));

    public static final RegistryObject<Block> ENDERNIUM_ORE = BLOCKS.register("endernium_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.END_STONE)
                    .strength(6f, 1200f).requiresCorrectToolForDrops(), UniformInt.of(3, 6)));

    // Register BlockItems here
    public static final RegistryObject<Item> ENDERNIUM_BLOCK_ITEM = ITEMS.register("endernium_block",
            () -> new BlockItem(ENDERNIUM_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> ENDERNIUM_ORE_ITEM = ITEMS.register("endernium_ore",
            () -> new BlockItem(ENDERNIUM_ORE.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}
