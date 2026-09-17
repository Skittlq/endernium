package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.item.EnderniumTooltipProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.function.Consumer;

public class EnderniumHelmet extends Item implements EnderniumTooltipProvider {
    public EnderniumHelmet(Properties properties) {
        super(properties.humanoidArmor(ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL, ArmorType.HELMET).fireResistant());
    }

    @Override
    public void appendEnderniumTooltip(ItemStack stack, Consumer<Component> tooltipAdder) {
        EnderniumArmorTooltipHelper.appendFullSetAbilityTooltip(tooltipAdder);
    }
}
