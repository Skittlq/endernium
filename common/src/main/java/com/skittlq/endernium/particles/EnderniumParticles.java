package com.skittlq.endernium.particles;

import net.minecraft.core.particles.SimpleParticleType;

import java.util.Objects;
import java.util.function.Supplier;

public enum EnderniumParticles {
    ENDERNIUM_SWEEP("endernium_sweep"),
    ENDERNIUM_BIT("endernium_bit"),
    REVERSE_ENDERNIUM_BIT("reverse_endernium_bit");

    private final String id;
    private Supplier<? extends SimpleParticleType> supplier = () -> {
        throw new IllegalStateException("Endernium particle has not been bound to a loader registry yet");
    };

    EnderniumParticles(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public void bind(Supplier<? extends SimpleParticleType> supplier) {
        this.supplier = Objects.requireNonNull(supplier);
    }

    public SimpleParticleType get() {
        return supplier.get();
    }
}
