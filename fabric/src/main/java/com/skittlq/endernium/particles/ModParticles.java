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
    public static final SimpleParticleType ENDERNIUM_SWEEP = register("endernium_sweep");
    public static final SimpleParticleType ENDERNIUM_BIT = register("endernium_bit");
    public static final SimpleParticleType REVERSE_ENDERNIUM_BIT = register("reverse_endernium_bit");

    private ModParticles() {
    }

    private static SimpleParticleType register(String name) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath(Endernium.MOD_ID, name), FabricParticleTypes.simple(true));
    }

    public static void register() {
        Endernium.LOGGER.info("Registering particles for {}", Endernium.MOD_ID);
    }

    public static void registerClient() {
        ParticleProviderRegistry.getInstance().register(ENDERNIUM_SWEEP, EnderniumSweep.Provider::new);
        ParticleProviderRegistry.getInstance().register(ENDERNIUM_BIT, EnderniumBit.Provider::new);
        ParticleProviderRegistry.getInstance().register(REVERSE_ENDERNIUM_BIT, ReverseEnderniumBit.Provider::new);
    }
}


