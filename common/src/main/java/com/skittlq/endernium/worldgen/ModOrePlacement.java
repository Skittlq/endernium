package com.skittlq.endernium.worldgen;

import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.ArrayList;
import java.util.List;

public final class ModOrePlacement {
    private ModOrePlacement() {
    }

    public static List<PlacementModifier> orePlacement(PlacementModifier countPlacement, PlacementModifier heightRange, PlacementModifier... extraModifiers) {
        List<PlacementModifier> modifiers = new ArrayList<>();
        modifiers.add(countPlacement);
        modifiers.add(InSquarePlacement.spread());
        modifiers.add(heightRange);
        modifiers.addAll(List.of(extraModifiers));
        modifiers.add(BiomeFilter.biome());
        return List.copyOf(modifiers);
    }

    public static List<PlacementModifier> commonOrePlacement(int count, PlacementModifier heightRange, PlacementModifier... extraModifiers) {
        return orePlacement(CountPlacement.of(count), heightRange, extraModifiers);
    }
}