package com.skittlq.endernium.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class DragonDefeatedPlacementFilter extends PlacementFilter {
    public static final DragonDefeatedPlacementFilter INSTANCE = new DragonDefeatedPlacementFilter();
    public static final MapCodec<DragonDefeatedPlacementFilter> CODEC = MapCodec.unit(INSTANCE);

    private DragonDefeatedPlacementFilter() {
    }

    public static DragonDefeatedPlacementFilter dragonDefeated() {
        return INSTANCE;
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        ServerLevel level = context.getLevel().getLevel();
        ServerLevel endLevel = level.getServer().getLevel(Level.END);
        if (endLevel == null) {
            return false;
        }

        var dragonFight = endLevel.getDragonFight();
        return dragonFight != null && dragonFight.hasPreviouslyKilledDragon();
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModPlacementModifiers.DRAGON_DEFEATED.get();
    }
}
