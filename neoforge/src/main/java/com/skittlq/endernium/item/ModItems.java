package com.skittlq.endernium.item;

import com.skittlq.endernium.Endernium;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Endernium.MODID);

    public static final DeferredItem<Item> ENDERNIUM_DUST = registerItem(EnderniumItems.ENDERNIUM_DUST);
    public static final DeferredItem<Item> ENDERNIUM_SHARD = registerItem(EnderniumItems.ENDERNIUM_SHARD);
    public static final DeferredItem<Item> ENDERNIUM_INGOT = registerItem(EnderniumItems.ENDERNIUM_INGOT);
    public static final DeferredItem<Item> ENDERNIUM_UPGRADE_SMITHING_TEMPLATE = registerItem(EnderniumItems.ENDERNIUM_UPGRADE_SMITHING_TEMPLATE);
    public static final DeferredItem<Item> ENDERNIUM_SWORD = registerItem(EnderniumItems.ENDERNIUM_SWORD);
    public static final DeferredItem<Item> ENDERNIUM_SPEAR = registerItem(EnderniumItems.ENDERNIUM_SPEAR);
    public static final DeferredItem<Item> ENDERNIUM_PICKAXE = registerItem(EnderniumItems.ENDERNIUM_PICKAXE);
    public static final DeferredItem<Item> ENDERNIUM_SHOVEL = registerItem(EnderniumItems.ENDERNIUM_SHOVEL);
    public static final DeferredItem<Item> ENDERNIUM_AXE = registerItem(EnderniumItems.ENDERNIUM_AXE);
    public static final DeferredItem<Item> ENDERNIUM_HOE = registerItem(EnderniumItems.ENDERNIUM_HOE);
    public static final DeferredItem<Item> ENDERNIUM_HELMET = registerItem(EnderniumItems.ENDERNIUM_HELMET);
    public static final DeferredItem<Item> ENDERNIUM_CHESTPLATE = registerItem(EnderniumItems.ENDERNIUM_CHESTPLATE);
    public static final DeferredItem<Item> ENDERNIUM_LEGGINGS = registerItem(EnderniumItems.ENDERNIUM_LEGGINGS);
    public static final DeferredItem<Item> ENDERNIUM_BOOTS = registerItem(EnderniumItems.ENDERNIUM_BOOTS);
    public static final DeferredItem<Item> ENDERNIUM_HORSE_ARMOR = registerItem(EnderniumItems.ENDERNIUM_HORSE_ARMOR);
    public static final DeferredItem<Item> ENDERNIUM_NAUTILUS_ARMOR = registerItem(EnderniumItems.ENDERNIUM_NAUTILUS_ARMOR);

    private static DeferredItem<Item> registerItem(EnderniumItems item) {
        DeferredItem<Item> registered = ITEMS.registerItem(item.id(), item::create);
        item.bind(registered);
        return registered;
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}