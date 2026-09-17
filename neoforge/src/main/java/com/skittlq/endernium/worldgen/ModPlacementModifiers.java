package com.skittlq.endernium.worldgen;

import com.mojang.serialization.MapCodec;
import com.skittlq.endernium.Endernium;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModPlacementModifiers {
    public static final DeferredRegister<MapCodec<? extends PlacementModifier>> PLACEMENT_MODIFIER_TYPES =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, Endernium.MODID);

    public static final DeferredHolder<MapCodec<? extends PlacementModifier>, MapCodec<DragonDefeatedPlacementFilter>> DRAGON_DEFEATED =
            registerDragonDefeated();

    private ModPlacementModifiers() {
    }

    private static DeferredHolder<MapCodec<? extends PlacementModifier>, MapCodec<DragonDefeatedPlacementFilter>> registerDragonDefeated() {
        return PLACEMENT_MODIFIER_TYPES.register(
                EnderniumPlacementModifiers.DRAGON_DEFEATED_ID,
                () -> DragonDefeatedPlacementFilter.CODEC
        );
    }

    public static void register(IEventBus eventBus) {
        PLACEMENT_MODIFIER_TYPES.register(eventBus);
    }
}
