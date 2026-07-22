package com.skittlq.endernium.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    private ModTags() {
    }

    public static final class Blocks {
        private Blocks() {
        }
        public static final TagKey<Block> NEEDS_ENDERNIUM_TOOL = createTag("needs_endernium_tool");
        public static final TagKey<Block> INCORRECT_FOR_ENDERNIUM_TOOL = createTag("incorrect_for_endernium_tool");

        private static TagKey<Block> createTag(String name) {
            return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("endernium", name));
        }
    }

    public static final class Items {
        private Items() {
        }
        public static final TagKey<Item> TRANSFORMABLE_ITEMS = createTag("transformable_items");
        public static final TagKey<Item> ENDERNIUM_REPAIRABLE = createTag("endernium_repairable");
        public static final TagKey<Item> INGOTS_ENDERNIUM = createTag("ingots/endernium");

        private static TagKey<Item> createTag(String name) {
            return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("endernium", name));
        }
    }
}

