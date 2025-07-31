package com.skittlq.endernium;

import com.mojang.logging.LogUtils;
import com.skittlq.endernium.advancement.ModCriteriaTriggers;
import com.skittlq.endernium.block.ModBlocks;
import com.skittlq.endernium.item.ModCreativeModeTabs;
import com.skittlq.endernium.item.ModItems;
import com.skittlq.endernium.loot.ModLootModifiers;
import com.skittlq.endernium.network.ModNetworking;
import com.skittlq.endernium.particles.ModParticles;
import com.skittlq.endernium.particles.custom.EnderniumBit;
import com.skittlq.endernium.particles.custom.EnderniumSweep;
import com.skittlq.endernium.particles.custom.ReverseEnderniumBit;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Endernium.MODID)
public class Endernium {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "endernium";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Endernium(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Endernium) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        MinecraftForge.EVENT_BUS.register(this);

        ModCreativeModeTabs.register(modEventBus); // Register the creative mode tabs to the mod event bus
        ModItems.register(modEventBus); // Register the items in ModItems to the mod event bus
        ModBlocks.register(modEventBus); // Register the blocks in ModItems to the mod event bus
        ModLootModifiers.register(modEventBus);
        ModParticles.register(modEventBus);
        ModCriteriaTriggers.register();

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("sigma endernium armor");

        event.enqueueWork(ModNetworking::register);
    }

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

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

        @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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
