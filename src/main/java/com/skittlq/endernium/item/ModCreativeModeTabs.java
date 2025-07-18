package com.skittlq.endernium.item;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Endernium.MODID);

    public static final Supplier<CreativeModeTab> ENDERNIUM_TAB = CREATIVE_MODE_TAB.register("endernium_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.ENDERNIUM_INGOT.get()))
                    .title(Component.translatable("creativetab.endernium"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.ENDERNIUM_INGOT.get());
                        output.accept(ModItems.ENDERNIUM_SHARD.get());
                        output.accept(ModItems.ENDERNIUM_DUST.get());
                        output.accept(ModBlocks.ENDERNIUM_BLOCK.get());
                        output.accept(ModBlocks.ENDERNIUM_ORE.get());
                        output.accept(ModItems.ENDERNIUM_AXE.get());
                        output.accept(ModItems.ENDERNIUM_HOE.get());
                        output.accept(ModItems.ENDERNIUM_PICKAXE.get());
                        output.accept(ModItems.ENDERNIUM_SHOVEL.get());
                        output.accept(ModItems.ENDERNIUM_SWORD.get());
                        output.accept(ModItems.ENDERNIUM_UPGRADE_SMITHING_TEMPLATE.get());
                        output.accept(ModItems.ENDERNIUM_HELMET.get());
                        output.accept(ModItems.ENDERNIUM_CHESTPLATE.get());
                        output.accept(ModItems.ENDERNIUM_LEGGINGS.get());
                        output.accept(ModItems.ENDERNIUM_BOOTS.get());
                    }
                ).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }

}
