package com.skittlq.endernium.advancement;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCriteriaTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(BuiltInRegistries.TRIGGER_TYPES, "endernium");

    // Use Supplier, not RegistryObject
    public static final Supplier<CriterionTrigger<?>> SWORD_SWEEP =
            TRIGGERS.register("sword_sweep", () -> EnderniumSwordSweepTrigger.INSTANCE);

    public static void register(IEventBus eventBus) {
        TRIGGERS.register(eventBus);
    }
}
