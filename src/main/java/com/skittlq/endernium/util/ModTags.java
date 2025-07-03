package com.skittlq.endernium.util;

import com.skittlq.endernium.Endernium;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> NEEDS_ENDERNIUM_TOOL = createTag("needs_endernium_tool");
        public static final TagKey<Block> INCORRECT_FOR_ENDERNIUM_TOOL = createTag("incorrect_for_endernium_tool");

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(Endernium.MODID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> TRANSFORMABLE_ITEMS = createTag("transformable_items");
        public static final TagKey<Item> ENDERNIUM_REPAIRABLE = createTag("endernium_repairable");
        public static final TagKey<Item> INGOTS_ENDERNIUM = createTag("ingots/endernium");

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(Endernium.MODID, name));
        }
    }
}
