package com.skittlq.endernium.item;

import com.skittlq.endernium.Endernium;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class ModItems {
    public static final Item ENDERNIUM_DUST = registerItem(EnderniumItems.ENDERNIUM_DUST);
    public static final Item ENDERNIUM_SHARD = registerItem(EnderniumItems.ENDERNIUM_SHARD);
    public static final Item ENDERNIUM_INGOT = registerItem(EnderniumItems.ENDERNIUM_INGOT);
    public static final Item ENDERNIUM_UPGRADE_SMITHING_TEMPLATE = registerItem(EnderniumItems.ENDERNIUM_UPGRADE_SMITHING_TEMPLATE);
    public static final Item ENDERNIUM_SWORD = registerItem(EnderniumItems.ENDERNIUM_SWORD);
    public static final Item ENDERNIUM_SPEAR = registerItem(EnderniumItems.ENDERNIUM_SPEAR);
    public static final Item ENDERNIUM_SHOVEL = registerItem(EnderniumItems.ENDERNIUM_SHOVEL);
    public static final Item ENDERNIUM_PICKAXE = registerItem(EnderniumItems.ENDERNIUM_PICKAXE);
    public static final Item ENDERNIUM_AXE = registerItem(EnderniumItems.ENDERNIUM_AXE);
    public static final Item ENDERNIUM_HOE = registerItem(EnderniumItems.ENDERNIUM_HOE);
    public static final Item ENDERNIUM_HELMET = registerItem(EnderniumItems.ENDERNIUM_HELMET);
    public static final Item ENDERNIUM_CHESTPLATE = registerItem(EnderniumItems.ENDERNIUM_CHESTPLATE);
    public static final Item ENDERNIUM_LEGGINGS = registerItem(EnderniumItems.ENDERNIUM_LEGGINGS);
    public static final Item ENDERNIUM_BOOTS = registerItem(EnderniumItems.ENDERNIUM_BOOTS);
    public static final Item ENDERNIUM_HORSE_ARMOR = registerItem(EnderniumItems.ENDERNIUM_HORSE_ARMOR);
    public static final Item ENDERNIUM_NAUTILUS_ARMOR = registerItem(EnderniumItems.ENDERNIUM_NAUTILUS_ARMOR);

    public static Item registerItem(EnderniumItems item) {
        Item registered = registerItem(item.id(), item::create);
        item.bind(() -> registered);
        return registered;
    }

    public static Item registerItem(String name, Function<Item.Properties, Item> function) {
        Identifier id = Identifier.fromNamespaceAndPath(Endernium.MOD_ID, name);
        return Registry.register(BuiltInRegistries.ITEM, id,
                function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    }

    public static void register() {
        Endernium.LOGGER.info("Registering Mod Items for " + Endernium.MOD_ID);
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
                .register(output -> EnderniumCreativeTabContents.acceptIngredients(item -> output.accept(item)));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS)
                .register(output -> EnderniumCreativeTabContents.acceptBuildingBlocks(item -> output.accept(item)));
    }
}