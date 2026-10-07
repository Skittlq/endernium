package com.skittlq.endernium;

import com.skittlq.endernium.attachment.ModAttachments;
import com.skittlq.endernium.advancement.ModCriteriaTriggerRegistrar;
import com.skittlq.endernium.block.ModBlocks;
import com.skittlq.endernium.config.EnderniumConfigManager;
import com.skittlq.endernium.config.EnderniumGameplayConfig;
import com.skittlq.endernium.combat.EnderniumCombatEvents;
import com.skittlq.endernium.item.ModCreativeModeTabs;
import com.skittlq.endernium.entity.ModEntities;
import com.skittlq.endernium.item.ModItems;
import com.skittlq.endernium.item.armor.EnderniumArmorAbilityHandler;
import com.skittlq.endernium.item.tools.EnderniumSwordCooldownHandler;
import com.skittlq.endernium.item.tools.EnderniumSpearStateHandler;
import com.skittlq.endernium.loot.ModLootConditions;
import com.skittlq.endernium.loot.ModLootModifiers;
import com.skittlq.endernium.network.ModNetworking;
import com.skittlq.endernium.particles.ModParticles;
import com.skittlq.endernium.progression.EnderniumBlessingHandler;
import com.skittlq.endernium.progression.EnderniumBlessingCommand;
import com.skittlq.endernium.util.EnderniumTickSchedulerEvents;
import com.skittlq.endernium.util.EnderniumUtilsEvents;
import com.skittlq.endernium.util.EnderniumUtils;
import com.skittlq.endernium.vfx.DragonDeathVfxDebugCommand;
import com.skittlq.endernium.worldgen.ModFeatures;
import com.skittlq.endernium.worldgen.ModPlacementModifiers;
import com.skittlq.endernium.worldgen.ModWorldgen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Endernium implements ModInitializer {
    public static final String MOD_ID = "endernium";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        EnderniumConfigManager.load();
        bindGameplayConfig();
        EnderniumUtils.bindSafeBlockBreaker((player, pos) -> player.gameMode.destroyBlock(pos));
        ModAttachments.initialize();
        ModCreativeModeTabs.registerModCreativeModeTabs();
        ModBlocks.register();
        ModEntities.register();
        ModItems.register();
        ModParticles.register();
        ModNetworking.register();
        EnderniumBlessingHandler.register();
        EnderniumCombatEvents.register();
        ModCriteriaTriggerRegistrar.register();
        ModFeatures.register();
        ModPlacementModifiers.register();
        ModWorldgen.register();
        ModLootConditions.register();
        ModLootModifiers.register();
        EnderniumTickSchedulerEvents.register();
        EnderniumUtilsEvents.register();
        EnderniumArmorAbilityHandler.register();
        EnderniumSwordCooldownHandler.register();
        EnderniumSpearStateHandler.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                EnderniumBlessingCommand.register(dispatcher));
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                    DragonDeathVfxDebugCommand.register(dispatcher));
        }
    }
    private static void bindGameplayConfig() {
        EnderniumGameplayConfig.bind(new EnderniumGameplayConfig.Settings() {
            @Override
            public boolean swordAbilityEnabled() {
                return EnderniumConfigManager.getConfig().enderniumSwordAbility;
            }

            @Override
            public double swordDamagePerStrikeMultiplier() {
                return EnderniumConfigManager.getConfig().enderniumSwordDamagePerStrikeMultiplier;
            }

            @Override
            public int swordMaxStrikes() {
                return EnderniumConfigManager.getConfig().enderniumSwordMaxStrikes;
            }

            @Override
            public boolean toolsVeinMiningEnabled() {
                return EnderniumConfigManager.getConfig().enderniumToolsVeinMining;
            }

            @Override
            public boolean armorAbilityEnabled() {
                return EnderniumConfigManager.getConfig().enderniumArmorAbility;
            }

            @Override
            public int armorAbilityThreshold() {
                return EnderniumConfigManager.getConfig().enderniumArmorAbilityThreshold;
            }

            @Override
            public double armorMaxStoredDamage() {
                return EnderniumConfigManager.getConfig().enderniumArmorMaxStoredDamage;
            }

            @Override
            public int spearStrainDurationSeconds() {
                return EnderniumConfigManager.getConfig().enderniumSpearStrainDurationSeconds;
            }

            @Override
            public double spearMaximumHealthCostPercent() {
                return EnderniumConfigManager.getConfig().enderniumSpearMaximumHealthCostPercent;
            }
        });
    }
}
