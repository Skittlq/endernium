package com.skittlq.endernium.item;

import com.skittlq.endernium.Endernium;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;

public class ModCreativeModeTabs {
    public static final CreativeModeTab ENDERNIUM_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "endernium"),
            FabricCreativeModeTab.builder().icon(EnderniumCreativeTabContents::icon)
                    .title(Component.translatable("creativetab.endernium"))
                    .displayItems((parameters, output) ->
                            EnderniumCreativeTabContents.acceptModTab(item -> output.accept(item))
                    ).build());

    public static void registerModCreativeModeTabs() {
        Endernium.LOGGER.info("Registering Creative Mode Tabs for " + Endernium.MOD_ID);
    }
}