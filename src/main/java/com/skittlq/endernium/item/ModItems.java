package com.skittlq.endernium.item;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.item.armor.*;
import com.skittlq.endernium.item.tools.EnderniumSword;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Endernium.MODID);

    public static final RegistryObject<Item> ENDERNIUM_DUST = ITEMS.register("endernium_dust",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ENDERNIUM_SHARD = ITEMS.register("endernium_shard",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ENDERNIUM_INGOT = ITEMS.register("endernium_ingot",
            () -> new Item(new Item.Properties()));
    public static SmithingTemplateItem createEnderniumUpgradeTemplate() {
        return new SmithingTemplateItem(
                Component.translatable("upgrade.minecraft.endernium_upgrade.applies_to"),
                Component.translatable("upgrade.minecraft.endernium_upgrade.ingredients"),
                Component.translatable("upgrade.minecraft.endernium_upgrade.upgrade"), // your custom upgrade description
                Component.translatable("upgrade.minecraft.endernium_upgrade.base_slot_description"),
                Component.translatable("upgrade.minecraft.endernium_upgrade.additions_slot_description"),
                List.of(
                        ResourceLocation.fromNamespaceAndPath("minecraft", "item/empty_armor_slot_helmet"),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "item/empty_armor_slot_chestplate"),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "item/empty_armor_slot_leggings"),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "item/empty_armor_slot_boots"),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "item/empty_slot_sword"),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "item/empty_slot_axe"),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "item/empty_slot_shovel"),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "item/empty_slot_pickaxe"),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "item/empty_slot_hoe")
                ),
                List.of(ResourceLocation.fromNamespaceAndPath("minecraft", "item/empty_slot_ingot"))
        );
    }

    public static final RegistryObject<Item> ENDERNIUM_UPGRADE_SMITHING_TEMPLATE =
            ITEMS.register("endernium_upgrade_smithing_template", ModItems::createEnderniumUpgradeTemplate);

    public static final RegistryObject<Item> ENDERNIUM_SWORD = ITEMS.register("endernium_sword",
            () -> new EnderniumSword(ModToolTiers.ENDERNIUM, 3, -2.4F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ENDERNIUM_PICKAXE = ITEMS.register("endernium_pickaxe",
            () -> new PickaxeItem(ModToolTiers.ENDERNIUM, 1, -2.8F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ENDERNIUM_SHOVEL = ITEMS.register("endernium_shovel",
            () -> new ShovelItem(ModToolTiers.ENDERNIUM, 1.5F, -3.0F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ENDERNIUM_AXE = ITEMS.register("endernium_axe",
            () -> new AxeItem(ModToolTiers.ENDERNIUM, 5.0F, -3.0F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> ENDERNIUM_HOE = ITEMS.register("endernium_hoe",
            () -> new HoeItem(ModToolTiers.ENDERNIUM, -4, 0.0F, new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> ENDERNIUM_HELMET = ITEMS.register("endernium_helmet",
            () -> new EnderniumHelmet(ModArmorMaterial.ENDERNIUM, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> ENDERNIUM_CHESTPLATE = ITEMS.register("endernium_chestplate",
            () -> new EnderniumChestplate(ModArmorMaterial.ENDERNIUM, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> ENDERNIUM_LEGGINGS = ITEMS.register("endernium_leggings",
            () -> new EnderniumLeggings(ModArmorMaterial.ENDERNIUM, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> ENDERNIUM_BOOTS = ITEMS.register("endernium_boots",
            () -> new EnderniumBoots(ModArmorMaterial.ENDERNIUM, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
