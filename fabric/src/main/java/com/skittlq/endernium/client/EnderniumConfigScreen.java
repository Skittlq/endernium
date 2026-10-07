package com.skittlq.endernium.client;

import com.skittlq.endernium.config.EnderniumConfig;
import com.skittlq.endernium.config.EnderniumConfigManager;
import com.skittlq.endernium.config.EnderniumGameplayConfig;
import com.skittlq.endernium.client.vfx.EnderniumVfxRenderMode;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class EnderniumConfigScreen {
    private EnderniumConfigScreen() {
    }

    public static Screen create(Screen parent) {
        EnderniumConfig config = EnderniumConfigManager.copyConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("endernium.config.title"));
        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("endernium.config.category.general"));
        ConfigCategory visuals = builder.getOrCreateCategory(Component.translatable("endernium.config.category.visuals"));

        visuals.addEntry(entries.startEnumSelector(
                        Component.translatable("endernium.config.vfx_render_mode"),
                        EnderniumVfxRenderMode.class,
                        config.vfxRenderMode)
                .setDefaultValue(EnderniumVfxRenderMode.AUTO)
                .setEnumNameProvider(value -> Component.translatable(
                        "endernium.config.vfx_render_mode." + value.name().toLowerCase(java.util.Locale.ROOT)))
                .setTooltip(Component.translatable("endernium.config.vfx_render_mode.tooltip"))
                .setSaveConsumer(value -> config.vfxRenderMode = value)
                .build());

        general.addEntry(entries.startBooleanToggle(
                        Component.translatable("endernium.config.armor_ability"),
                        config.enderniumArmorAbility)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("endernium.config.armor_ability.tooltip"))
                .setSaveConsumer(value -> config.enderniumArmorAbility = value)
                .build());

        general.addEntry(entries.startIntField(
                        Component.translatable("endernium.config.armor_ability_threshold"),
                        config.enderniumArmorAbilityThreshold)
                .setDefaultValue(4)
                .setMin(1)
                .setTooltip(Component.translatable("endernium.config.armor_ability_threshold.tooltip"))
                .setSaveConsumer(value -> config.enderniumArmorAbilityThreshold = value)
                .build());

        general.addEntry(entries.startDoubleField(
                        Component.translatable("endernium.config.armor_max_stored_damage"),
                        config.enderniumArmorMaxStoredDamage)
                .setDefaultValue(EnderniumGameplayConfig.DEFAULT_ARMOR_MAX_STORED_DAMAGE)
                .setMin(1.0D)
                .setMax(EnderniumGameplayConfig.MAX_ARMOR_STORED_DAMAGE)
                .setTooltip(Component.translatable("endernium.config.armor_max_stored_damage.tooltip"))
                .setSaveConsumer(value -> config.enderniumArmorMaxStoredDamage = value)
                .build());


        general.addEntry(entries.startBooleanToggle(
                        Component.translatable("endernium.config.sword_ability"),
                        config.enderniumSwordAbility)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("endernium.config.sword_ability.tooltip"))
                .setSaveConsumer(value -> config.enderniumSwordAbility = value)
                .build());

        general.addEntry(entries.startDoubleField(
                        Component.translatable("endernium.config.sword_damage_per_strike"),
                        config.enderniumSwordDamagePerStrikeMultiplier)
                .setDefaultValue(EnderniumGameplayConfig.DEFAULT_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER)
                .setMin(EnderniumGameplayConfig.MIN_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER)
                .setMax(EnderniumGameplayConfig.MAX_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER)
                .setTooltip(Component.translatable("endernium.config.sword_damage_per_strike.tooltip"))
                .setSaveConsumer(value -> config.enderniumSwordDamagePerStrikeMultiplier = value)
                .build());

        general.addEntry(entries.startIntField(
                        Component.translatable("endernium.config.sword_max_strikes"),
                        config.enderniumSwordMaxStrikes)
                .setDefaultValue(EnderniumGameplayConfig.DEFAULT_SWORD_MAX_STRIKES)
                .setMin(1)
                .setMax(EnderniumGameplayConfig.MAX_SWORD_STRIKES)
                .setTooltip(Component.translatable("endernium.config.sword_max_strikes.tooltip"))
                .setSaveConsumer(value -> config.enderniumSwordMaxStrikes = value)
                .build());

        general.addEntry(entries.startIntField(
                        Component.translatable("endernium.config.spear_strain_duration"),
                        config.enderniumSpearStrainDurationSeconds)
                .setDefaultValue(EnderniumGameplayConfig.DEFAULT_SPEAR_STRAIN_DURATION_SECONDS)
                .setMin(1)
                .setMax(EnderniumGameplayConfig.MAX_SPEAR_STRAIN_DURATION_SECONDS)
                .setTooltip(Component.translatable("endernium.config.spear_strain_duration.tooltip"))
                .setSaveConsumer(value -> config.enderniumSpearStrainDurationSeconds = value)
                .build());

        general.addEntry(entries.startDoubleField(
                        Component.translatable("endernium.config.spear_health_cost"),
                        config.enderniumSpearMaximumHealthCostPercent)
                .setDefaultValue(EnderniumGameplayConfig.DEFAULT_SPEAR_MAXIMUM_HEALTH_COST_PERCENT)
                .setMin(0.0D)
                .setMax(100.0D)
                .setTooltip(Component.translatable("endernium.config.spear_health_cost.tooltip"))
                .setSaveConsumer(value -> config.enderniumSpearMaximumHealthCostPercent = value)
                .build());

        general.addEntry(entries.startBooleanToggle(
                        Component.translatable("endernium.config.tools_vein_mining"),
                        config.enderniumToolsVeinMining)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("endernium.config.tools_vein_mining.tooltip"))
                .setSaveConsumer(value -> config.enderniumToolsVeinMining = value)
                .build());

        builder.setSavingRunnable(() -> {
            EnderniumConfigManager.setConfig(config);
            EnderniumConfigManager.save();
        });

        return builder.build();
    }
}
