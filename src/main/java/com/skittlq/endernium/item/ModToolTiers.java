package com.skittlq.endernium.item;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.block.ModBlocks;
import com.skittlq.endernium.util.ModTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.List;
import java.util.function.Supplier;
import java.util.Set;

public class ModToolTiers {
    public static final Tier ENDERNIUM = TierSortingRegistry.registerTier(
            new ForgeTier(5, 3058, 12F, 5F, 20,
                    ModTags.Blocks.NEEDS_ENDERNIUM_TOOL, () -> Ingredient.of(ModItems.ENDERNIUM_INGOT.get())),
            new ResourceLocation(Endernium.MODID, "endernium"), List.of(Tiers.NETHERITE), List.of());

}