package com.skittlq.endernium;

import com.skittlq.endernium.block.ModBlocks;
import com.skittlq.endernium.item.ModCreativeModeTabs;
import com.skittlq.endernium.item.ModItems;
import com.skittlq.endernium.loot.ModLootModifiers;
import com.skittlq.endernium.particles.ModParticles;
import com.skittlq.endernium.particles.custom.EnderniumBit;
import com.skittlq.endernium.particles.custom.EnderniumSweep;
import com.skittlq.endernium.particles.custom.ReverseEnderniumBit;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Endernium.MODID)
public class Endernium {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "endernium";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Endernium(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Endernium) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        ModCreativeModeTabs.register(modEventBus); // Register the creative mode tabs to the mod event bus
        ModItems.register(modEventBus); // Register the items in ModItems to the mod event bus
        ModBlocks.register(modEventBus); // Register the blocks in ModItems to the mod event bus
        ModLootModifiers.register(modEventBus);
        ModParticles.register(modEventBus);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("sigma endernium armor");
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.ENDERNIUM_DUST);
            event.accept(ModItems.ENDERNIUM_SHARD);
            event.accept(ModItems.ENDERNIUM_INGOT);
        } else if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModBlocks.ENDERNIUM_BLOCK);
            event.accept(ModBlocks.ENDERNIUM_ORE);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
        @EventBusSubscriber(value = Dist.CLIENT, modid = MODID)
        public static class ClientModEvents {
            @SubscribeEvent
            public static void onClientSetup(FMLClientSetupEvent event) {
            }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.ENDERNIUM_SWEEP.get(), EnderniumSweep.Provider::new);
            event.registerSpriteSet(ModParticles.ENDERNIUM_BIT.get(), EnderniumBit.Provider::new);
            event.registerSpriteSet(ModParticles.REVERSE_ENDERNIUM_BIT.get(), ReverseEnderniumBit.Provider::new);
        }

    }
}
