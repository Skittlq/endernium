package com.skittlq.endernium.worldgen;

import com.skittlq.endernium.Endernium;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModPlacementModifiers {
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, Endernium.MODID);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<DragonDefeatedPlacementFilter>> DRAGON_DEFEATED =
            registerDragonDefeated();

    private ModPlacementModifiers() {
    }

    private static DeferredHolder<PlacementModifierType<?>, PlacementModifierType<DragonDefeatedPlacementFilter>> registerDragonDefeated() {
        DeferredHolder<PlacementModifierType<?>, PlacementModifierType<DragonDefeatedPlacementFilter>> holder =
                PLACEMENT_MODIFIER_TYPES.register(EnderniumPlacementModifiers.DRAGON_DEFEATED_ID,
                        () -> () -> DragonDefeatedPlacementFilter.CODEC);
        EnderniumPlacementModifiers.bindDragonDefeated(holder);
        return holder;
    }

    public static PlacementModifierType<DragonDefeatedPlacementFilter> dragonDefeatedType() {
        return EnderniumPlacementModifiers.dragonDefeatedType();
    }

    public static void register(IEventBus eventBus) {
        PLACEMENT_MODIFIER_TYPES.register(eventBus);
    }
}