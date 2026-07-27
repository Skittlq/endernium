package com.skittlq.endernium;

import com.skittlq.endernium.config.EnderniumGameplayConfig;
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
                .defineInRange("enderniumArmorAbilityCooldown", 90, 1, Long.MAX_VALUE);


        public static final ModConfigSpec.BooleanValue ENDERNIUM_SWORD_ABILITY = BUILDER
                .comment("Whether the Endernium Sword ability can be activated.")
                .define("enderniumSwordAbility", true);

        public static final ModConfigSpec.IntValue ENDERNIUM_SWORD_ABILITY_BASE_COOLDOWN = BUILDER
                .comment("Base cooldown for the Endernium Sword ability, in seconds.")
                .defineInRange("enderniumSwordAbilityBaseCooldown", EnderniumGameplayConfig.DEFAULT_SWORD_ABILITY_BASE_COOLDOWN_SECONDS, 0, Integer.MAX_VALUE);

        public static final ModConfigSpec.IntValue ENDERNIUM_SWORD_ABILITY_PER_MOB_COOLDOWN = BUILDER
                .comment("Additional cooldown per mob hit by the Endernium Sword ability, in seconds.")
                .defineInRange("enderniumSwordAbilityPerMobCooldown", EnderniumGameplayConfig.DEFAULT_SWORD_ABILITY_PER_MOB_COOLDOWN_SECONDS, 0, Integer.MAX_VALUE);

        public static final ModConfigSpec.BooleanValue ENDERNIUM_TOOLS_VEIN_MINING = BUILDER
                .comment("Whether Endernium tools can use vein mining.")
                .define("enderniumToolsVeinMining", true);

    static final ModConfigSpec SPEC = BUILDER.build();
    public static void bindGameplayConfig() {
        EnderniumGameplayConfig.bind(new EnderniumGameplayConfig.Settings() {
            @Override
            public boolean swordAbilityEnabled() {
                return ENDERNIUM_SWORD_ABILITY.getAsBoolean();
            }

            @Override
            public int swordAbilityBaseCooldownSeconds() {
                return ENDERNIUM_SWORD_ABILITY_BASE_COOLDOWN.getAsInt();
            }

            @Override
            public int swordAbilityPerMobCooldownSeconds() {
                return ENDERNIUM_SWORD_ABILITY_PER_MOB_COOLDOWN.getAsInt();
            }

            @Override
            public boolean toolsVeinMiningEnabled() {
                return ENDERNIUM_TOOLS_VEIN_MINING.getAsBoolean();
            }
        });
    }
}
