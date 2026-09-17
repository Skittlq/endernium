package com.skittlq.endernium.worldgen;

import com.mojang.serialization.MapCodec;
import com.skittlq.endernium.Endernium;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ModPlacementModifiers {
    public static final MapCodec<DragonDefeatedPlacementFilter> DRAGON_DEFEATED = registerDragonDefeated();

    private ModPlacementModifiers() {
    }

    private static MapCodec<DragonDefeatedPlacementFilter> registerDragonDefeated() {
        return Registry.register(
                BuiltInRegistries.PLACEMENT_MODIFIER_TYPE,
                Identifier.fromNamespaceAndPath(Endernium.MOD_ID, EnderniumPlacementModifiers.DRAGON_DEFEATED_ID),
                DragonDefeatedPlacementFilter.CODEC
        );
    }

    public static void register() {
    }
}
