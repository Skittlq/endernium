package com.skittlq.endernium.item.armor;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

import static com.skittlq.endernium.item.armor.ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL;

public class EnderniumHelmet extends Item {
    public EnderniumHelmet(Properties properties) {
        super(properties.humanoidArmor(ENDERNIUM_ARMOR_MATERIAL, ArmorType.HELMET).fireResistant());
    }
}
