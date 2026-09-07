package com.skittlq.endernium.item.armor;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.Equippable;

public final class EnderniumArmorUtil {
    private EnderniumArmorUtil() {
    }

    public static boolean hasFullEnderniumSet(LivingEntity entity) {
        ArmorMaterial material = ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL;
        return isEnderniumPiece(entity.getItemBySlot(EquipmentSlot.HEAD), material)
                && isEnderniumPiece(entity.getItemBySlot(EquipmentSlot.CHEST), material)
                && isEnderniumPiece(entity.getItemBySlot(EquipmentSlot.LEGS), material)
                && isEnderniumPiece(entity.getItemBySlot(EquipmentSlot.FEET), material);
    }

    private static boolean isEnderniumPiece(ItemStack stack, ArmorMaterial material) {
        if (stack.isEmpty()) {
            return false;
        }
        Equippable equippable = stack.getComponents().get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.assetId().filter(id -> id.equals(material.assetId())).isPresent();
    }
}
