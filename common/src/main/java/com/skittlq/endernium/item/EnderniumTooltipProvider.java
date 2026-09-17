package com.skittlq.endernium.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public interface EnderniumTooltipProvider {
    void appendEnderniumTooltip(ItemStack stack, Consumer<Component> tooltipAdder);

    static void appendTooltip(ItemStack stack, List<Component> tooltip) {
        if (stack.getItem() instanceof EnderniumTooltipProvider provider) {
            provider.appendEnderniumTooltip(stack, tooltip::add);
        }
    }
}
