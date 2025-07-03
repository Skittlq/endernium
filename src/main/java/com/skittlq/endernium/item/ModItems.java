package com.skittlq.endernium.item;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.item.tools.EnderniumSword;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Endernium.MODID);

    public static final DeferredItem<Item> ENDERNIUM_DUST = ITEMS.registerSimpleItem("endernium_dust", new Item.Properties());
    public static final DeferredItem<Item> ENDERNIUM_SHARD = ITEMS.registerSimpleItem("endernium_shard", new Item.Properties());
    public static final DeferredItem<Item> ENDERNIUM_INGOT = ITEMS.registerSimpleItem("endernium_ingot", new Item.Properties());
    public static final DeferredItem<Item> ENDERNIUM_UPGRADE_SMITHING_TEMPLATE = ITEMS.registerItem("endernium_upgrade_smithing_template",
            SmithingTemplateItem::createNetheriteUpgradeTemplate);
//    public static final DeferredItem<Item> VOID_BAG = ITEMS.registerItem("void_bag",
//            (properties) -> new Item(properties.stacksTo(1).component(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY)));
    public static final DeferredItem<Item> ENDERNIUM_SWORD = ITEMS.registerItem("endernium_sword",
            (properties) -> new EnderniumSword(properties.sword(ModToolTiers.ENDERNIUM, 3.0F, -2.4F).fireResistant()));
    public static final DeferredItem<Item> ENDERNIUM_PICKAXE = ITEMS.registerItem("endernium_pickaxe",
            (properties) -> new Item(properties.pickaxe(ModToolTiers.ENDERNIUM, 1.0F, -2.8F).fireResistant()));
    public static final DeferredItem<ShovelItem> ENDERNIUM_SHOVEL = ITEMS.registerItem("endernium_shovel",
            (properties) -> new ShovelItem(ModToolTiers.ENDERNIUM, 1.5F, -3.0F, properties.fireResistant()));
    public static final DeferredItem<AxeItem> ENDERNIUM_AXE = ITEMS.registerItem("endernium_axe",
            (properties) -> new AxeItem(ModToolTiers.ENDERNIUM, 5.0F, -3.0F, properties.fireResistant()));
    public static final DeferredItem<HoeItem> ENDERNIUM_HOE = ITEMS.registerItem("endernium_hoe",
            (properties) -> new HoeItem(ModToolTiers.ENDERNIUM, -4.0F, 0.0F, properties.fireResistant()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
