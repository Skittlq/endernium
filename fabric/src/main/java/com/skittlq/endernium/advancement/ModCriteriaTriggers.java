package com.skittlq.endernium.advancement;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModCriteriaTriggers {
    private static boolean registered;

    private ModCriteriaTriggers() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        Registry.register(BuiltInRegistries.TRIGGER_TYPES, EnderniumSwordSweepTrigger.ID, EnderniumSwordSweepTrigger.INSTANCE);
    }
}
