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

    public static final Supplier<SimpleParticleType> ENDERNIUM_SWEEP = register(EnderniumParticles.ENDERNIUM_SWEEP);
    public static final Supplier<SimpleParticleType> ENDERNIUM_BIT = register(EnderniumParticles.ENDERNIUM_BIT);
    public static final Supplier<SimpleParticleType> REVERSE_ENDERNIUM_BIT = register(EnderniumParticles.REVERSE_ENDERNIUM_BIT);

    private static Supplier<SimpleParticleType> register(EnderniumParticles definition) {
        Supplier<SimpleParticleType> particle = PARTICLE_TYPES.register(definition.id(), () -> new SimpleParticleType(true));
        definition.bind(particle);
        return particle;
    }

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }

    public static SimpleParticleType enderniumSweepParticle() {
        return EnderniumParticles.ENDERNIUM_SWEEP.get();
    }

    public static SimpleParticleType reverseEnderniumBitParticle() {
        return EnderniumParticles.REVERSE_ENDERNIUM_BIT.get();
    }
}