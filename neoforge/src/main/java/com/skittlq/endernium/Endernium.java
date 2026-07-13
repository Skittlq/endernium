package com.skittlq.endernium;

import com.skittlq.endernium.advancement.ModCriteriaTriggers;
import com.skittlq.endernium.block.ModBlocks;
import com.skittlq.endernium.item.ModCreativeModeTabs;
import com.skittlq.endernium.item.ModItems;
import com.skittlq.endernium.loot.ModLootModifiers;
import com.skittlq.endernium.particles.ModParticles;
import com.skittlq.endernium.particles.custom.EnderniumBit;
import com.skittlq.endernium.particles.custom.EnderniumSweep;
import com.skittlq.endernium.particles.custom.ReverseEnderniumBit;
import com.skittlq.endernium.worldgen.ModPlacementModifiers;
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
    public static final String MODID = "endernium";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Endernium(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);

        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        ModParticles.register(modEventBus);
        ModCriteriaTriggers.register(modEventBus);
        ModPlacementModifiers.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("sigma endernium armor");
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
