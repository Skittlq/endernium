package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.client.EnderniumKeyBindings;
import com.skittlq.endernium.progression.EnderniumBlessing;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public final class EnderniumNautilusArmor extends Item {
    public EnderniumNautilusArmor(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> tooltipAdder,
            TooltipFlag flag
    ) {
        if (!EnderniumBlessing.isClientBlessed()) {
            tooltipAdder.accept(Component.translatable("endernium.tooltip.ability.locked")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltipAdder.accept(Component.translatable("endernium.tooltip.nautilus_armor.title")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipAdder.accept(Component.empty());
            tooltipAdder.accept(Component.translatable(
                    "endernium.tooltip.nautilus_armor.activate",
                    EnderniumKeyBindings.abilityKeyName()
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipAdder.accept(Component.translatable(
                    "endernium.tooltip.nautilus_armor.cooldown",
                    EnderniumNautilusArmorAbility.COOLDOWN_TICKS / 20
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipAdder.accept(Component.translatable("endernium.tooltip.nautilus_armor.description")
                    .withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
    }
}
