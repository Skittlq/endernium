package com.skittlq.endernium.particles;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.particles.custom.EnderniumBit;
import com.skittlq.endernium.particles.custom.EnderniumSweep;
import com.skittlq.endernium.particles.custom.ReverseEnderniumBit;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ModParticles {
    public static final SimpleParticleType ENDERNIUM_SWEEP = register(EnderniumParticles.ENDERNIUM_SWEEP);
    public static final SimpleParticleType ENDERNIUM_BIT = register(EnderniumParticles.ENDERNIUM_BIT);
    public static final SimpleParticleType REVERSE_ENDERNIUM_BIT = register(EnderniumParticles.REVERSE_ENDERNIUM_BIT);

    private ModParticles() {
    }

    private static SimpleParticleType register(EnderniumParticles definition) {
        SimpleParticleType particle = Registry.register(
                BuiltInRegistries.PARTICLE_TYPE,
                Identifier.fromNamespaceAndPath(Endernium.MOD_ID, definition.id()),
                FabricParticleTypes.simple(true)
        );
        definition.bind(() -> particle);
        return particle;
    }

    public static void register() {
        Endernium.LOGGER.info("Registering particles for {}", Endernium.MOD_ID);
    }

    public static void registerClient() {
        ParticleProviderRegistry.getInstance().register(ENDERNIUM_SWEEP, EnderniumSweep.Provider::new);
        ParticleProviderRegistry.getInstance().register(ENDERNIUM_BIT, EnderniumBit.Provider::new);
        ParticleProviderRegistry.getInstance().register(REVERSE_ENDERNIUM_BIT, ReverseEnderniumBit.Provider::new);
    }

    public static SimpleParticleType enderniumSweepParticle() {
        return EnderniumParticles.ENDERNIUM_SWEEP.get();
    }

    public static SimpleParticleType reverseEnderniumBitParticle() {
        return EnderniumParticles.REVERSE_ENDERNIUM_BIT.get();
    }
}