package com.skittlq.endernium.item;

import com.skittlq.endernium.block.EnderniumBlocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

public final class EnderniumCreativeTabContents {
    private EnderniumCreativeTabContents() {
    }

    public static ItemStack icon() {
        return new ItemStack(EnderniumItems.ENDERNIUM_INGOT.get());
    }

    public static void acceptModTab(Consumer<ItemLike> output) {
        output.accept(EnderniumItems.ENDERNIUM_INGOT.get());
        output.accept(EnderniumItems.ENDERNIUM_SHARD.get());
        output.accept(EnderniumItems.ENDERNIUM_DUST.get());
        output.accept(EnderniumItems.ENDERNIUM_UPGRADE_SMITHING_TEMPLATE.get());
        output.accept(EnderniumBlocks.ENDERNIUM_BLOCK.block());
        output.accept(EnderniumBlocks.ENDERNIUM_ORE.block());
        output.accept(EnderniumItems.ENDERNIUM_SWORD.get());
        output.accept(EnderniumItems.ENDERNIUM_SPEAR.get());
        output.accept(EnderniumItems.ENDERNIUM_SHOVEL.get());
        output.accept(EnderniumItems.ENDERNIUM_PICKAXE.get());
        output.accept(EnderniumItems.ENDERNIUM_AXE.get());
        output.accept(EnderniumItems.ENDERNIUM_HOE.get());
        output.accept(EnderniumItems.ENDERNIUM_HELMET.get());
        output.accept(EnderniumItems.ENDERNIUM_CHESTPLATE.get());
        output.accept(EnderniumItems.ENDERNIUM_LEGGINGS.get());
        output.accept(EnderniumItems.ENDERNIUM_BOOTS.get());
        output.accept(EnderniumItems.ENDERNIUM_HORSE_ARMOR.get());
        output.accept(EnderniumItems.ENDERNIUM_NAUTILUS_ARMOR.get());
    }

    public static void acceptIngredients(Consumer<ItemLike> output) {
        output.accept(EnderniumItems.ENDERNIUM_DUST.get());
        output.accept(EnderniumItems.ENDERNIUM_SHARD.get());
        output.accept(EnderniumItems.ENDERNIUM_INGOT.get());
    }

    public static void acceptBuildingBlocks(Consumer<ItemLike> output) {
        output.accept(EnderniumBlocks.ENDERNIUM_BLOCK.block());
        output.accept(EnderniumBlocks.ENDERNIUM_ORE.block());
    }
}
