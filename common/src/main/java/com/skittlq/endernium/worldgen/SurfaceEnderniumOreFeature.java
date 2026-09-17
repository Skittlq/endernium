package com.skittlq.endernium.worldgen;

import com.mojang.serialization.MapCodec;
import com.skittlq.endernium.block.EnderniumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.AbstractOreFeature;
import net.minecraft.world.level.levelgen.feature.BlockReplacement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SurfaceEnderniumOreFeature extends AbstractOreFeature {
    public static final MapCodec<SurfaceEnderniumOreFeature> CODEC = makeCodec(SurfaceEnderniumOreFeature::new);

    public SurfaceEnderniumOreFeature(List<BlockReplacement> targetStates, int size, float discardChanceOnAirExposure) {
        super(targetStates, size, discardChanceOnAirExposure);
    }

    @Override
    public MapCodec<SurfaceEnderniumOreFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        List<BlockPos> candidates = new ArrayList<>();
        Set<BlockPos> queued = new HashSet<>();
        int placed = 0;

        candidates.add(origin.immutable());
        queued.add(origin.immutable());

        while (!candidates.isEmpty() && placed < size) {
            BlockPos pos = candidates.remove(random.nextInt(candidates.size()));
            BlockState oreState = oreStateFor(level, pos, random);
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

    private BlockState oreStateFor(WorldGenLevel level, BlockPos pos, RandomSource random) {
        BlockState currentState = level.getBlockState(pos);
        BlockPos.MutableBlockPos mutablePos = pos.mutable();
        for (BlockReplacement targetState : targetStates) {
            if (targetState.target().test(currentState, mutablePos, random)) {
                return targetState.state();
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
