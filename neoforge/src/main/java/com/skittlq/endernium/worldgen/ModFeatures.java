package com.skittlq.endernium.worldgen;

import com.skittlq.endernium.Endernium;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(BuiltInRegistries.FEATURE, Endernium.MODID);

    public static final DeferredHolder<Feature<?>, Feature<OreConfiguration>> SURFACE_ENDERNIUM_ORE =
            registerSurfaceEnderniumOre();

    private ModFeatures() {
    }

    private static DeferredHolder<Feature<?>, Feature<OreConfiguration>> registerSurfaceEnderniumOre() {
        DeferredHolder<Feature<?>, Feature<OreConfiguration>> holder = FEATURES.register(
                EnderniumFeatures.SURFACE_ENDERNIUM_ORE_ID,
                () -> new SurfaceEnderniumOreFeature(OreConfiguration.CODEC)
        );
        EnderniumFeatures.bindSurfaceEnderniumOre(holder);
        return holder;
    }

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}