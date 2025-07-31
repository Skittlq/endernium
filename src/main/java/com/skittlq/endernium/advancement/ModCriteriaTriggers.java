package com.skittlq.endernium.advancement;

import net.minecraft.advancements.CriterionTrigger;

public class ModCriteriaTriggers {
    // No DeferredRegister or RegistryObject required for triggers in 1.20.1.
    public static final EnderniumSwordSweepTrigger SWORD_SWEEP = EnderniumSwordSweepTrigger.INSTANCE;

    public static void register() {
        // Explicitly register trigger so it's known to Minecraft (mainly for in-game advancements)
        net.minecraft.advancements.CriteriaTriggers.register(SWORD_SWEEP);
    }
}
