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
                .defineInRange("enderniumArmorAbilityThreshold", 4, 1, 2048);

        public static final ModConfigSpec.DoubleValue ENDERNIUM_ARMOR_MAX_STORED_DAMAGE = BUILDER
                .comment("Maximum qualifying damage stored by the Endernium Armor ability.")
                .defineInRange("enderniumArmorMaxStoredDamage",
                        EnderniumGameplayConfig.DEFAULT_ARMOR_MAX_STORED_DAMAGE,
                        1.0D, EnderniumGameplayConfig.MAX_ARMOR_STORED_DAMAGE);


        public static final ModConfigSpec.BooleanValue ENDERNIUM_SWORD_ABILITY = BUILDER
                .comment("Whether the Endernium Sword ability can be activated.")
                .define("enderniumSwordAbility", true);

        public static final ModConfigSpec.DoubleValue ENDERNIUM_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER = BUILDER
                .comment("Normal sword damage required per stored Ender Barrage strike, as a multiplier.")
                .defineInRange("enderniumSwordDamagePerStrikeMultiplier",
                        EnderniumGameplayConfig.DEFAULT_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER,
                        EnderniumGameplayConfig.MIN_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER,
                        EnderniumGameplayConfig.MAX_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER);

        public static final ModConfigSpec.IntValue ENDERNIUM_SWORD_MAX_STRIKES = BUILDER
                .comment("Maximum strikes stored by the Endernium Sword ability.")
                .defineInRange("enderniumSwordMaxStrikes",
                        EnderniumGameplayConfig.DEFAULT_SWORD_MAX_STRIKES, 1,
                        EnderniumGameplayConfig.MAX_SWORD_STRIKES);

        public static final ModConfigSpec.IntValue ENDERNIUM_SPEAR_STRAIN_DURATION = BUILDER
                .comment("Duration of Endernium Spear strain after a successful teleport, in seconds.")
                .defineInRange("enderniumSpearStrainDurationSeconds",
                        EnderniumGameplayConfig.DEFAULT_SPEAR_STRAIN_DURATION_SECONDS, 1,
                        EnderniumGameplayConfig.MAX_SPEAR_STRAIN_DURATION_SECONDS);

        public static final ModConfigSpec.DoubleValue ENDERNIUM_SPEAR_MAXIMUM_HEALTH_COST = BUILDER
                .comment("Maximum percentage of max health paid for immediately reusing the Endernium Spear.")
                .defineInRange("enderniumSpearMaximumHealthCostPercent",
                        EnderniumGameplayConfig.DEFAULT_SPEAR_MAXIMUM_HEALTH_COST_PERCENT,
                        0.0D, 100.0D);

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
            public double swordDamagePerStrikeMultiplier() {
                return ENDERNIUM_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER.getAsDouble();
            }

            @Override
            public int swordMaxStrikes() {
                return ENDERNIUM_SWORD_MAX_STRIKES.getAsInt();
            }

            @Override
            public boolean toolsVeinMiningEnabled() {
                return ENDERNIUM_TOOLS_VEIN_MINING.getAsBoolean();
            }

            @Override
            public boolean armorAbilityEnabled() {
                return ENDERNIUM_ARMOR_ABILITY.getAsBoolean();
            }

            @Override
            public int armorAbilityThreshold() {
                return ENDERNIUM_ARMOR_ABILITY_THRESHOLD.getAsInt();
            }

            @Override
            public double armorMaxStoredDamage() {
                return ENDERNIUM_ARMOR_MAX_STORED_DAMAGE.getAsDouble();
            }

            @Override
            public int spearStrainDurationSeconds() {
                return ENDERNIUM_SPEAR_STRAIN_DURATION.getAsInt();
            }

            @Override
            public double spearMaximumHealthCostPercent() {
                return ENDERNIUM_SPEAR_MAXIMUM_HEALTH_COST.getAsDouble();
            }
        });
    }
}
