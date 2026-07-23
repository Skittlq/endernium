package com.skittlq.endernium;

import com.skittlq.endernium.attachment.ModAttachments;
import com.skittlq.endernium.advancement.ModCriteriaTriggers;
import com.skittlq.endernium.block.ModBlocks;
import com.skittlq.endernium.config.EnderniumConfigManager;
import com.skittlq.endernium.item.ModCreativeModeTabs;
import com.skittlq.endernium.item.ModItems;
import com.skittlq.endernium.item.armor.EnderniumArmorAbilityHandler;
import com.skittlq.endernium.loot.ModLootModifiers;
import com.skittlq.endernium.network.ModNetworking;
import com.skittlq.endernium.particles.ModParticles;
import com.skittlq.endernium.util.EnderniumTickSchedulerEvents;
import com.skittlq.endernium.util.EnderniumUtils;
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
        ModAttachments.initialize();
        ModCreativeModeTabs.registerModCreativeModeTabs();
        ModBlocks.register();
        ModItems.register();
        ModParticles.register();
        ModNetworking.register();
        ModCriteriaTriggers.register();
        ModPlacementModifiers.register();
        ModWorldgen.register();
        ModLootModifiers.register();
        EnderniumTickSchedulerEvents.register();
        EnderniumUtils.register();
        EnderniumArmorAbilityHandler.register();
    }
}
