package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.item.EnderniumTooltipProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.function.Consumer;

public class EnderniumBoots extends Item implements EnderniumTooltipProvider {
    public EnderniumBoots(Properties properties) {
        super(properties.humanoidArmor(ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL, ArmorType.BOOTS).fireResistant());
    }

    @Override
    public void appendEnderniumTooltip(ItemStack stack, Consumer<Component> tooltipAdder) {
        EnderniumArmorTooltipHelper.appendFullSetAbilityTooltip(tooltipAdder);
    }
}
