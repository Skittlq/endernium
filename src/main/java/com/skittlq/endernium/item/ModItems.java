package com.skittlq.endernium.item;

import com.skittlq.endernium.Endernium;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Endernium.MODID);

    public static final DeferredItem<Item> ENDERNIUM_DUST = ITEMS.registerSimpleItem("endernium_dust", new Item.Properties());
    public static final DeferredItem<Item> ENDERNIUM_SHARD = ITEMS.registerSimpleItem("endernium_shard", new Item.Properties());
    public static final DeferredItem<Item> ENDERNIUM_INGOT = ITEMS.registerSimpleItem("endernium_ingot", new Item.Properties());

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
