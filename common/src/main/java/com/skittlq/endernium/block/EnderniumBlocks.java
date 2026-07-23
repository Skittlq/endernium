package com.skittlq.endernium.block;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public enum EnderniumBlocks {
    ENDERNIUM_BLOCK("endernium_block", properties -> new Block(
            properties.strength(55.0F).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)
    )),
    ENDERNIUM_ORE("endernium_ore", properties -> new DropExperienceBlock(
            UniformInt.of(2, 4),
            properties.requiresCorrectToolForDrops().strength(12.5F, 1600.0F).sound(SoundType.AMETHYST)
    ));

    private final String id;
    private final Function<BlockBehaviour.Properties, Block> factory;
    private Supplier<? extends Block> blockSupplier = () -> {
        throw new IllegalStateException("Endernium block has not been bound to a loader registry yet");
    };
    private Supplier<? extends Item> itemSupplier = () -> {
        throw new IllegalStateException("Endernium block item has not been bound to a loader registry yet");
    };

    EnderniumBlocks(String id, Function<BlockBehaviour.Properties, Block> factory) {
        this.id = id;
        this.factory = factory;
    }

    public String id() {
        return id;
    }

    public Block create(BlockBehaviour.Properties properties) {
        return factory.apply(properties);
    }

    public void bindBlock(Supplier<? extends Block> supplier) {
        this.blockSupplier = Objects.requireNonNull(supplier);
    }

    public void bindItem(Supplier<? extends Item> supplier) {
        this.itemSupplier = Objects.requireNonNull(supplier);
    }

    public Block block() {
        return blockSupplier.get();
    }

    public Item item() {
        return itemSupplier.get();
    }
}
