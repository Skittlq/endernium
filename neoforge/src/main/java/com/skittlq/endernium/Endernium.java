package com.skittlq.endernium;

import com.skittlq.endernium.advancement.ModCriteriaTriggerRegistrar;
import com.skittlq.endernium.block.ModBlocks;
import com.skittlq.endernium.combat.EnderniumCombatEvents;
import com.skittlq.endernium.item.EnderniumCreativeTabContents;
import com.skittlq.endernium.item.ModCreativeModeTabs;
import com.skittlq.endernium.item.ModItems;
import com.skittlq.endernium.item.armor.EnderniumArmorAbilityHandler;
import com.skittlq.endernium.item.tools.EnderniumSwordCooldownHandler;
import com.skittlq.endernium.loot.ModLootConditions;
import com.skittlq.endernium.loot.ModLootModifiers;
import com.skittlq.endernium.particles.EnderniumParticles;
import com.skittlq.endernium.particles.ModParticles;
import com.skittlq.endernium.particles.custom.EnderniumBit;
import com.skittlq.endernium.particles.custom.EnderniumSweep;
import com.skittlq.endernium.particles.custom.ReverseEnderniumBit;
import com.skittlq.endernium.client.vfx.EnderniumShaderRenderer;
import com.skittlq.endernium.vfx.DragonDeathVfxDebugCommand;
import com.skittlq.endernium.worldgen.ModFeatures;
import com.skittlq.endernium.worldgen.ModPlacementModifiers;
import net.minecraft.SharedConstants;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(Endernium.MODID)
public class Endernium {
    public static final String MODID = "endernium";
    public Endernium(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);

        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        ModLootConditions.register(modEventBus);
        ModParticles.register(modEventBus);
        ModCriteriaTriggerRegistrar.register(modEventBus);
        ModFeatures.register(modEventBus);
        ModPlacementModifiers.register(modEventBus);
        EnderniumArmorAbilityHandler.register();
        EnderniumSwordCooldownHandler.register();
        EnderniumCombatEvents.register();

        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
        Config.bindGameplayConfig();
    }


    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            EnderniumCreativeTabContents.acceptIngredients(item -> event.accept(item));
        } else if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            EnderniumCreativeTabContents.acceptBuildingBlocks(item -> event.accept(item));
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @SubscribeEvent
    public void registerDevelopmentCommands(RegisterCommandsEvent event) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            DragonDeathVfxDebugCommand.register(event.getDispatcher());
        }
    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = MODID)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(EnderniumParticles.ENDERNIUM_SWEEP.get(), EnderniumSweep.Provider::new);
            event.registerSpriteSet(EnderniumParticles.ENDERNIUM_BIT.get(), EnderniumBit.Provider::new);
            event.registerSpriteSet(EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(), ReverseEnderniumBit.Provider::new);
        }

        @SubscribeEvent
        public static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
            EnderniumShaderRenderer.pipelines().forEach(event::registerPipeline);
        }

        @SubscribeEvent
        public static void registerReloadListeners(AddClientReloadListenersEvent event) {
            event.addListener(Identifier.fromNamespaceAndPath(MODID, "shader_framework_reload"),
                    (ResourceManagerReloadListener) manager -> {
                        EnderniumVfxManager.clear();
                        EnderniumShaderRenderer.instance().resetBuffers();
                    });
        }
    }
}
