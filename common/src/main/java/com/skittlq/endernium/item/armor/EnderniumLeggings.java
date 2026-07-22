package com.skittlq.endernium.item.armor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.function.Consumer;

public class EnderniumLeggings extends Item {
    public EnderniumLeggings(Properties properties) {
        super(properties.humanoidArmor(ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL, ArmorType.LEGGINGS).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipAdder, TooltipFlag flag) {
        EnderniumArmorTooltipHelper.appendFullSetAbilityTooltip(context, tooltipAdder);
        super.appendHoverText(stack, context, display, tooltipAdder, flag);
    }
}