package com.skittlq.endernium.entity;

import net.minecraft.world.entity.EntityType;

import java.util.Objects;
import java.util.function.Supplier;

public final class EnderniumEntityTypes {
    private static Supplier<EntityType<EnderniumThrownSpear>> thrownSpear = () -> {
        throw new IllegalStateException("Endernium thrown spear entity type has not been registered");
    };

    private EnderniumEntityTypes() {
    }

    public static void bindThrownSpear(Supplier<EntityType<EnderniumThrownSpear>> supplier) {
        thrownSpear = Objects.requireNonNull(supplier);
    }

    public static EntityType<EnderniumThrownSpear> thrownSpear() {
        return thrownSpear.get();
    }
}
