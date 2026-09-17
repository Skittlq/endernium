package com.skittlq.endernium.worldgen;

import com.mojang.serialization.MapCodec;
import com.skittlq.endernium.Endernium;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModFeatures {
    public static final DeferredRegister<MapCodec<? extends Feature>> FEATURES =
            DeferredRegister.create(BuiltInRegistries.FEATURE_TYPE, Endernium.MODID);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<SurfaceEnderniumOreFeature>> SURFACE_ENDERNIUM_ORE =
            registerSurfaceEnderniumOre();

    private ModFeatures() {
    }

    private static DeferredHolder<MapCodec<? extends Feature>, MapCodec<SurfaceEnderniumOreFeature>> registerSurfaceEnderniumOre() {
        return FEATURES.register(
                EnderniumFeatures.SURFACE_ENDERNIUM_ORE_ID,
                () -> SurfaceEnderniumOreFeature.CODEC
        );
    }

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
