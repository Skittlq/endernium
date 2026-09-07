package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.config.EnderniumGameplayConfig;
import com.skittlq.endernium.client.EnderniumClientGameplaySettings;
import com.skittlq.endernium.client.EnderniumClientEquipmentState;
import com.skittlq.endernium.progression.EnderniumAwakening;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

final class EnderniumArmorTooltipHelper {
    private EnderniumArmorTooltipHelper() {
    }

    static void appendFullSetAbilityTooltip(Consumer<Component> tooltipAdder) {
        if (!EnderniumClientEquipmentState.isFullEnderniumSetEquipped()) {
            return;
        }

        EnderniumGameplayConfig.Snapshot settings = EnderniumClientGameplaySettings.get();
        if (!settings.armorAbilityEnabled()) {
            return;
        }

        if (!EnderniumAwakening.isClientAwakened()) {
            tooltipAdder.accept(Component.translatable("endernium.tooltip.ability.locked")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltipAdder.accept(Component.translatable("endernium.tooltip.armor_ability.title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipAdder.accept(Component.empty());
        tooltipAdder.accept(Component.translatable(
                "endernium.tooltip.armor_ability.trigger",
                settings.armorThreshold()
        ).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipAdder.accept(Component.translatable(
                "endernium.tooltip.armor_ability.cooldown",
                settings.armorCooldownSeconds()
        ).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipAdder.accept(Component.translatable("endernium.tooltip.armor_ability.description").withStyle(ChatFormatting.GRAY));
    }

}
