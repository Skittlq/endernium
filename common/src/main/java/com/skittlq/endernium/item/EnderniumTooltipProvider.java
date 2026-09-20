package com.skittlq.endernium.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface EnderniumTooltipProvider {
    void appendEnderniumTooltip(ItemStack stack, Consumer<Component> tooltipAdder);

    static void appendTooltip(ItemStack stack, List<Component> tooltip) {
        if (stack.getItem() instanceof EnderniumTooltipProvider provider) {
            List<Component> enderniumLines = new ArrayList<>();
            provider.appendEnderniumTooltip(stack, enderniumLines::add);
            tooltip.addAll(Math.min(1, tooltip.size()), enderniumLines);
        }
    }
}
