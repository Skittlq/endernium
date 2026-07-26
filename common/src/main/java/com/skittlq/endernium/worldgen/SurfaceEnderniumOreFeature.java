package com.skittlq.endernium.worldgen;

import com.mojang.serialization.Codec;
import com.skittlq.endernium.block.EnderniumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SurfaceEnderniumOreFeature extends Feature<OreConfiguration> {
    public SurfaceEnderniumOreFeature(Codec<OreConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        OreConfiguration configuration = context.config();
        List<BlockPos> candidates = new ArrayList<>();
        Set<BlockPos> queued = new HashSet<>();
        int placed = 0;

        candidates.add(context.origin().immutable());
        queued.add(context.origin().immutable());

        while (!candidates.isEmpty() && placed < configuration.size) {
            BlockPos pos = candidates.remove(random.nextInt(candidates.size()));
            BlockState oreState = oreStateFor(level, pos, random, configuration);
            if (oreState == null || !isSurfaceConnected(level, pos, oreState)) {
                continue;
            }

            level.setBlock(pos, oreState, 2);
            placed++;

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = pos.relative(direction);
                if (queued.add(neighbor.immutable()) && random.nextFloat() < 0.85F) {
                    candidates.add(neighbor.immutable());
                }
            }
        }

        return placed > 0;
    }

    private static BlockState oreStateFor(WorldGenLevel level, BlockPos pos, RandomSource random, OreConfiguration configuration) {
        BlockState currentState = level.getBlockState(pos);
        for (OreConfiguration.TargetBlockState targetState : configuration.targetStates) {
            if (targetState.target.test(currentState, random)) {
                return targetState.state;
            }
        }
        return null;
    }

    private static boolean isSurfaceConnected(WorldGenLevel level, BlockPos pos, BlockState oreState) {
        for (Direction direction : Direction.values()) {
            BlockState neighborState = level.getBlockState(pos.relative(direction));
            if (neighborState.isAir() || neighborState.is(oreState.getBlock()) || neighborState.is(EnderniumBlocks.ENDERNIUM_ORE.block())) {
                return true;
            }
        }
        return false;
    }
}
