package com.skittlq.endernium.item;

import com.skittlq.endernium.Endernium;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Endernium.MODID);

    public static final Supplier<CreativeModeTab> ENDERNIUM_TAB = CREATIVE_MODE_TAB.register("endernium_tab",
            () -> CreativeModeTab.builder().icon(EnderniumCreativeTabContents::icon)
                    .title(Component.translatable("creativetab.endernium"))
                    .displayItems((itemDisplayParameters, output) ->
                            EnderniumCreativeTabContents.acceptModTab(item -> output.accept(item))
                    ).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}