package com.skittlq.endernium;

import com.skittlq.endernium.attachment.ModAttachments;
import com.skittlq.endernium.advancement.ModCriteriaTriggerRegistrar;
import com.skittlq.endernium.block.ModBlocks;
import com.skittlq.endernium.config.EnderniumConfigManager;
import com.skittlq.endernium.config.EnderniumGameplayConfig;
import com.skittlq.endernium.item.ModCreativeModeTabs;
import com.skittlq.endernium.item.ModItems;
import com.skittlq.endernium.item.armor.EnderniumArmorAbilityHandler;
import com.skittlq.endernium.loot.ModLootConditions;
import com.skittlq.endernium.loot.ModLootModifiers;
import com.skittlq.endernium.network.ModNetworking;
import com.skittlq.endernium.particles.ModParticles;
import com.skittlq.endernium.util.EnderniumTickSchedulerEvents;
import com.skittlq.endernium.util.EnderniumUtilsEvents;
import com.skittlq.endernium.worldgen.ModFeatures;
import com.skittlq.endernium.worldgen.ModPlacementModifiers;
import com.skittlq.endernium.worldgen.ModWorldgen;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Endernium implements ModInitializer {
    public static final String MOD_ID = "endernium";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        EnderniumConfigManager.load();
        bindGameplayConfig();
        ModAttachments.initialize();
        ModCreativeModeTabs.registerModCreativeModeTabs();
        ModBlocks.register();
        ModItems.register();
        ModParticles.register();
        ModNetworking.register();
        ModCriteriaTriggerRegistrar.register();
        ModFeatures.register();
        ModPlacementModifiers.register();
        ModWorldgen.register();
        ModLootConditions.register();
        ModLootModifiers.register();
        EnderniumTickSchedulerEvents.register();
        EnderniumUtilsEvents.register();
        EnderniumArmorAbilityHandler.register();
    }
    private static void bindGameplayConfig() {
        EnderniumGameplayConfig.bind(new EnderniumGameplayConfig.Settings() {
            @Override
            public boolean swordAbilityEnabled() {
                return EnderniumConfigManager.getConfig().enderniumSwordAbility;
            }

            @Override
            public int swordAbilityBaseCooldownSeconds() {
                return EnderniumConfigManager.getConfig().enderniumSwordAbilityBaseCooldown;
            }

            @Override
            public int swordAbilityPerMobCooldownSeconds() {
                return EnderniumConfigManager.getConfig().enderniumSwordAbilityPerMobCooldown;
            }

            @Override
            public boolean toolsVeinMiningEnabled() {
                return EnderniumConfigManager.getConfig().enderniumToolsVeinMining;
            }
        });
    }
}
