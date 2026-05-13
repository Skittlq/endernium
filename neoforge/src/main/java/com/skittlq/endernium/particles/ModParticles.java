package com.skittlq.endernium.particles;

import com.skittlq.endernium.Endernium;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Endernium.MODID);

    public static final Supplier<SimpleParticleType> ENDERNIUM_SWEEP =
            PARTICLE_TYPES.register("endernium_sweep", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> ENDERNIUM_BIT =
            PARTICLE_TYPES.register("endernium_bit", () -> new SimpleParticleType(true));
    public static final Supplier<SimpleParticleType> REVERSE_ENDERNIUM_BIT =
            PARTICLE_TYPES.register("reverse_endernium_bit", () -> new SimpleParticleType(true));


    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }

}
