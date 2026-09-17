package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.item.EnderniumTooltipProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.function.Consumer;

public class EnderniumLeggings extends Item implements EnderniumTooltipProvider {
    public EnderniumLeggings(Properties properties) {
        super(properties.humanoidArmor(ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL, ArmorType.LEGGINGS).fireResistant());
    }

    @Override
    public void appendEnderniumTooltip(ItemStack stack, Consumer<Component> tooltipAdder) {
        EnderniumArmorTooltipHelper.appendFullSetAbilityTooltip(tooltipAdder);
    }
}
