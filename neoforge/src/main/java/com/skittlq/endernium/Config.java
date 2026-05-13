package com.skittlq.endernium;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

        public static final ModConfigSpec.BooleanValue ENDERNIUM_ARMOR_ABILITY = BUILDER
            .comment("Whether the Endernium Armor has a special ability that triggers when the player is low on health.")
            .define("enderniumArmorAbility", true);

        public static final ModConfigSpec.IntValue ENDERNIUM_ARMOR_ABILITY_THRESHOLD = BUILDER
                .comment("At what health the Endernium Armor ability should trigger.")
                .defineInRange("enderniumArmorAbilityThreshold", 4, 1, Integer.MAX_VALUE);

        public static final ModConfigSpec.LongValue ENDERNIUM_ARMOR_ABILITY_COOLDOWN = BUILDER
                .comment("How long the Endernium Armor ability should be on cooldown, in seconds.")
                .defineInRange("enderniumArmorAbilityCooldown", 120, 1, Long.MAX_VALUE);

        // TODO: ADD MORE CONFIG OPTIONS FOR ENDERNIUM SWORD, PICKAXE, SHOVEL, AXE, AND HOE

    static final ModConfigSpec SPEC = BUILDER.build();
}
