package com.skittlq.endernium.client;

import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Supplier;

public final class EnderniumKeyBindings {
    private static Supplier<Component> abilityKeyName = () -> Component.translatable("key.endernium.activate_ability");
    private static Supplier<Component> sneakKeyName = () -> Component.translatable("key.sneak");

    private EnderniumKeyBindings() {
    }

    public static void bindAbilityKeyName(Supplier<Component> supplier) {
        abilityKeyName = Objects.requireNonNull(supplier);
    }

    public static Component abilityKeyName() {
        return abilityKeyName.get();
    }

    public static void bindSneakKeyName(Supplier<Component> supplier) {
        sneakKeyName = Objects.requireNonNull(supplier);
    }

    public static Component sneakKeyName() {
        return sneakKeyName.get();
    }
}
