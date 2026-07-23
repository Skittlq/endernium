package com.skittlq.endernium.worldgen;

import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Objects;
import java.util.function.Supplier;

public final class EnderniumPlacementModifiers {
    public static final String DRAGON_DEFEATED_ID = "dragon_defeated";

    private static Supplier<? extends PlacementModifierType<DragonDefeatedPlacementFilter>> dragonDefeated = () -> {
        throw new IllegalStateException("dragon_defeated placement modifier has not been bound to a loader registry yet");
    };

    private EnderniumPlacementModifiers() {
    }

    public static void bindDragonDefeated(Supplier<? extends PlacementModifierType<DragonDefeatedPlacementFilter>> supplier) {
        dragonDefeated = Objects.requireNonNull(supplier);
    }

    public static PlacementModifierType<DragonDefeatedPlacementFilter> dragonDefeatedType() {
        return dragonDefeated.get();
    }
}
