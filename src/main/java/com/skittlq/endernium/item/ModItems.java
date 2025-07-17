package com.skittlq.endernium.item;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.item.armor.EnderniumBoots;
import com.skittlq.endernium.item.armor.EnderniumChestplate;
import com.skittlq.endernium.item.armor.EnderniumHelmet;
import com.skittlq.endernium.item.armor.EnderniumLeggings;
import com.skittlq.endernium.item.tools.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

import static com.skittlq.endernium.item.armor.ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL;


public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Endernium.MODID);

    public static final DeferredItem<Item> ENDERNIUM_DUST = ITEMS.registerSimpleItem("endernium_dust", new Item.Properties());
    public static final DeferredItem<Item> ENDERNIUM_SHARD = ITEMS.registerSimpleItem("endernium_shard", new Item.Properties());
    public static final DeferredItem<Item> ENDERNIUM_INGOT = ITEMS.registerSimpleItem("endernium_ingot", new Item.Properties().fireResistant());
    public static SmithingTemplateItem createEnderniumUpgradeTemplate(Item.Properties properties) {
        return new SmithingTemplateItem(
                Component.translatable("upgrade.minecraft.endernium_upgrade.applies_to"),
                Component.translatable("upgrade.minecraft.endernium_upgrade.ingredients"),
                Component.translatable("upgrade.minecraft.endernium_upgrade.base_slot_description"),
                Component.translatable("upgrade.minecraft.endernium_upgrade.additions_slot_description"),
                List.of(
                        ResourceLocation.withDefaultNamespace("container/slot/helmet"),
                        ResourceLocation.withDefaultNamespace("container/slot/chestplate"),
                        ResourceLocation.withDefaultNamespace("container/slot/leggings"),
                        ResourceLocation.withDefaultNamespace("container/slot/boots"),
                        ResourceLocation.withDefaultNamespace("container/slot/sword"),
                        ResourceLocation.withDefaultNamespace("container/slot/axe"),
                        ResourceLocation.withDefaultNamespace("container/slot/shovel"),
                        ResourceLocation.withDefaultNamespace("container/slot/pickaxe"),
                        ResourceLocation.withDefaultNamespace("container/slot/hoe")
                ),
                List.of(
                        ResourceLocation.withDefaultNamespace("container/slot/ingot")
                ),
                properties
        );
    }

    public static final DeferredItem<Item> ENDERNIUM_UPGRADE_SMITHING_TEMPLATE =
            ITEMS.registerItem("endernium_upgrade_smithing_template", ModItems::createEnderniumUpgradeTemplate);

    //    public static final DeferredItem<Item> VOID_BAG = ITEMS.registerItem("void_bag",
//            (properties) -> new Item(properties.stacksTo(1).component(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY)));
    public static final DeferredItem<Item> ENDERNIUM_SWORD = ITEMS.registerItem("endernium_sword",
            (properties) -> new EnderniumSword(properties.sword(ModToolTiers.ENDERNIUM, 3.0F, -2.4F).fireResistant()));
    public static final DeferredItem<Item> ENDERNIUM_PICKAXE = ITEMS.registerItem("endernium_pickaxe",
            (properties) -> new EnderniumPickaxe(properties.pickaxe(ModToolTiers.ENDERNIUM, 1.0F, -2.8F).fireResistant()));
    public static final DeferredItem<Item> ENDERNIUM_SHOVEL = ITEMS.registerItem("endernium_shovel",
            (properties) -> new EnderniumShovel(properties.shovel(ModToolTiers.ENDERNIUM, 1.5F, -3.0F).fireResistant()));
    public static final DeferredItem<Item> ENDERNIUM_AXE = ITEMS.registerItem("endernium_axe",
            (properties) -> new EnderniumAxe( properties.axe(ModToolTiers.ENDERNIUM, 5.0F, -3.0F).fireResistant()));
    public static final DeferredItem<Item> ENDERNIUM_HOE = ITEMS.registerItem("endernium_hoe",
            (properties) -> new EnderniumHoe(properties.hoe(ModToolTiers.ENDERNIUM, -4.0F, 0.0F).fireResistant()));

    public static final DeferredItem<Item> ENDERNIUM_HELMET = ITEMS.registerItem("endernium_helmet",
            EnderniumHelmet::new
            );
    public static final DeferredItem<Item> ENDERNIUM_CHESTPLATE = ITEMS.registerItem("endernium_chestplate",
            EnderniumChestplate::new
            );
    public static final DeferredItem<Item> ENDERNIUM_LEGGINGS = ITEMS.registerItem("endernium_leggings",
            EnderniumLeggings::new
            );
    public static final DeferredItem<Item> ENDERNIUM_BOOTS = ITEMS.registerItem("endernium_boots",
            EnderniumBoots::new
            );


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
