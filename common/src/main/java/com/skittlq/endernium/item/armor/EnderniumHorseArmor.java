package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.client.EnderniumKeyBindings;
import com.skittlq.endernium.item.EnderniumTooltipProvider;
import com.skittlq.endernium.progression.EnderniumBlessing;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public final class EnderniumHorseArmor extends Item implements EnderniumTooltipProvider {
    public EnderniumHorseArmor(Properties properties) {
        super(properties);
    }

    @Override
    public void appendEnderniumTooltip(
            ItemStack stack,
            Consumer<Component> tooltipAdder
    ) {
        if (!EnderniumBlessing.isClientBlessed()) {
            tooltipAdder.accept(Component.translatable("endernium.tooltip.ability.locked")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltipAdder.accept(Component.translatable("endernium.tooltip.horse_armor.title")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipAdder.accept(Component.empty());
            tooltipAdder.accept(Component.translatable("endernium.tooltip.horse_armor.passive")
                    .withStyle(ChatFormatting.GRAY));
            tooltipAdder.accept(Component.translatable(
                    "endernium.tooltip.horse_armor.activate",
                    EnderniumKeyBindings.abilityKeyName()
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipAdder.accept(Component.translatable(
                    "endernium.tooltip.horse_armor.cooldown",
                    EnderniumHorseArmorAbility.DASH_COOLDOWN_TICKS / 20
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipAdder.accept(Component.translatable("endernium.tooltip.horse_armor.description")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
