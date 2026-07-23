package com.skittlq.endernium.advancement;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModCriteriaTriggerRegistrar {
    private static boolean registered;

    private ModCriteriaTriggerRegistrar() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        Registry.register(BuiltInRegistries.TRIGGER_TYPES, EnderniumSwordSweepTrigger.ID, ModCriteriaTriggers.SWORD_SWEEP);
    }
}