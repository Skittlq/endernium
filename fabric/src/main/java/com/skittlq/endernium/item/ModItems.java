package com.skittlq.endernium.item;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.block.ModBlocks;
import com.skittlq.endernium.item.armor.EnderniumBoots;
import com.skittlq.endernium.item.armor.EnderniumChestplate;
import com.skittlq.endernium.item.armor.EnderniumHelmet;
import com.skittlq.endernium.item.armor.EnderniumLeggings;
import com.skittlq.endernium.item.tools.*;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;

import java.util.List;
import java.util.function.Function;

public class ModItems {

    public static final Item ENDERNIUM_INGOT = registerItem("endernium_ingot", properties -> new Item(properties.fireResistant()));
    public static final Item ENDERNIUM_SHARD = registerItem("endernium_shard", properties -> new Item(properties));
    public static final Item ENDERNIUM_DUST = registerItem("endernium_dust", properties -> new Item(properties));

    public static SmithingTemplateItem createEnderniumUpgradeTemplate(Item.Properties properties) {
        return new SmithingTemplateItem(
                Component.translatable("upgrade.minecraft.endernium_upgrade.applies_to"),
                Component.translatable("upgrade.minecraft.endernium_upgrade.ingredients"),
                Component.translatable("upgrade.minecraft.endernium_upgrade.base_slot_description"),
                Component.translatable("upgrade.minecraft.endernium_upgrade.additions_slot_description"),
                List.of(
                        Identifier.withDefaultNamespace("container/slot/helmet"),
                        Identifier.withDefaultNamespace("container/slot/chestplate"),
                        Identifier.withDefaultNamespace("container/slot/leggings"),
                        Identifier.withDefaultNamespace("container/slot/boots"),
                        Identifier.withDefaultNamespace("container/slot/sword"),
                        Identifier.withDefaultNamespace("container/slot/axe"),
                        Identifier.withDefaultNamespace("container/slot/shovel"),
                        Identifier.withDefaultNamespace("container/slot/pickaxe"),
                        Identifier.withDefaultNamespace("container/slot/hoe")
                ),
                List.of(
                        Identifier.withDefaultNamespace("container/slot/ingot")
                ),
                properties
        );
    }
    public static final Item ENDERNIUM_UPGRADE_SMITHING_TEMPLATE = registerItem("endernium_upgrade_smithing_template", ModItems::createEnderniumUpgradeTemplate);

    public static final Item ENDERNIUM_SWORD = registerItem("endernium_sword", properties -> new EnderniumSword(properties.fireResistant()));
    public static final Item ENDERNIUM_SHOVEL = registerItem("endernium_shovel", properties -> new EnderniumShovel(properties.fireResistant()));
    public static final Item ENDERNIUM_PICKAXE = registerItem("endernium_pickaxe", properties -> new EnderniumPickaxe(properties.fireResistant()));
    public static final Item ENDERNIUM_AXE = registerItem("endernium_axe", properties -> new EnderniumAxe(properties.fireResistant()));
    public static final Item ENDERNIUM_HOE = registerItem("endernium_hoe", properties -> new EnderniumHoe(properties.fireResistant()));
    public static final Item ENDERNIUM_HELMET = registerItem("endernium_helmet", properties -> new EnderniumHelmet(properties));
    public static final Item ENDERNIUM_CHESTPLATE = registerItem("endernium_chestplate", properties -> new EnderniumChestplate(properties));
    public static final Item ENDERNIUM_LEGGINGS = registerItem("endernium_leggings", properties -> new EnderniumLeggings(properties));
    public static final Item ENDERNIUM_BOOTS = registerItem("endernium_boots", properties -> new EnderniumBoots(properties));


    public static Item registerItem(String name, Function<Item.Properties, Item> function) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(Endernium.MOD_ID, name),
                function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Endernium.MOD_ID, name)))));
    }

    public static void register() {
        Endernium.LOGGER.info("Registering Mod Items for " + Endernium.MOD_ID);
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> {
            output.accept(ENDERNIUM_INGOT);
            output.accept(ENDERNIUM_SHARD);
            output.accept(ENDERNIUM_DUST);
        });
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(output -> {
            output.accept(ModBlocks.ENDERNIUM_BLOCK_ITEM);
            output.accept(ModBlocks.ENDERNIUM_ORE_ITEM);
        });
    }
}
