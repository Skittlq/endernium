package com.skittlq.endernium.worldgen;

import com.mojang.serialization.MapCodec;
import com.skittlq.endernium.util.EnderniumDragonState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;

public final class DragonDefeatedPlacementFilter implements PlacementFilter {
    public static final DragonDefeatedPlacementFilter INSTANCE = new DragonDefeatedPlacementFilter();
    public static final MapCodec<DragonDefeatedPlacementFilter> CODEC = MapCodec.unit(INSTANCE);

    private DragonDefeatedPlacementFilter() {
    }

    public static DragonDefeatedPlacementFilter dragonDefeated() {
        return INSTANCE;
    }

    @Override
    public boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        ServerLevel level = context.getLevel().getLevel();
        return EnderniumDragonState.hasDragonBeenDefeated(level);
    }

    @Override
    public MapCodec<DragonDefeatedPlacementFilter> codec() {
        return CODEC;
    }
}
