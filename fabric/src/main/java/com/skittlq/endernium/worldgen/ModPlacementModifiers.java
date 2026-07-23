package com.skittlq.endernium.worldgen;

import com.skittlq.endernium.Endernium;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class ModPlacementModifiers {
    public static final PlacementModifierType<DragonDefeatedPlacementFilter> DRAGON_DEFEATED = registerDragonDefeated();

    private ModPlacementModifiers() {
    }

    private static PlacementModifierType<DragonDefeatedPlacementFilter> registerDragonDefeated() {
        PlacementModifierType<DragonDefeatedPlacementFilter> type = Registry.register(
                BuiltInRegistries.PLACEMENT_MODIFIER_TYPE,
                Identifier.fromNamespaceAndPath(Endernium.MOD_ID, EnderniumPlacementModifiers.DRAGON_DEFEATED_ID),
                () -> DragonDefeatedPlacementFilter.CODEC
        );
        EnderniumPlacementModifiers.bindDragonDefeated(() -> type);
        return type;
    }

    public static PlacementModifierType<DragonDefeatedPlacementFilter> dragonDefeatedType() {
        return EnderniumPlacementModifiers.dragonDefeatedType();
    }

    public static void register() {
    }
}