package com.skittlq.endernium.client;

import com.skittlq.endernium.config.EnderniumConfig;
import com.skittlq.endernium.config.EnderniumConfigManager;
import com.skittlq.endernium.config.EnderniumGameplayConfig;
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

        general.addEntry(entries.startLongField(
                        Component.translatable("endernium.config.armor_ability_cooldown"),
                        config.enderniumArmorAbilityCooldown)
                .setDefaultValue(90L)
                .setMin(1L)
                .setTooltip(Component.translatable("endernium.config.armor_ability_cooldown.tooltip"))
                .setSaveConsumer(value -> config.enderniumArmorAbilityCooldown = value)
                .build());


        general.addEntry(entries.startBooleanToggle(
                        Component.translatable("endernium.config.sword_ability"),
                        config.enderniumSwordAbility)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("endernium.config.sword_ability.tooltip"))
                .setSaveConsumer(value -> config.enderniumSwordAbility = value)
                .build());

        general.addEntry(entries.startIntField(
                        Component.translatable("endernium.config.sword_ability_base_cooldown"),
                        config.enderniumSwordAbilityBaseCooldown)
                .setDefaultValue(EnderniumGameplayConfig.DEFAULT_SWORD_ABILITY_BASE_COOLDOWN_SECONDS)
                .setMin(0)
                .setTooltip(Component.translatable("endernium.config.sword_ability_base_cooldown.tooltip"))
                .setSaveConsumer(value -> config.enderniumSwordAbilityBaseCooldown = value)
                .build());

        general.addEntry(entries.startIntField(
                        Component.translatable("endernium.config.sword_ability_per_mob_cooldown"),
                        config.enderniumSwordAbilityPerMobCooldown)
                .setDefaultValue(EnderniumGameplayConfig.DEFAULT_SWORD_ABILITY_PER_MOB_COOLDOWN_SECONDS)
                .setMin(0)
                .setTooltip(Component.translatable("endernium.config.sword_ability_per_mob_cooldown.tooltip"))
                .setSaveConsumer(value -> config.enderniumSwordAbilityPerMobCooldown = value)
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
