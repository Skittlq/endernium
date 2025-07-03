package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.util.ModTags;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;

public class ModArmorMaterial {
    public static final ArmorMaterial ENDERNIUM_ARMOR_MATERIAL = new ArmorMaterial(
            40,
            Util.make(new EnumMap<>(ArmorType.class), map -> {
                map.put(ArmorType.BOOTS, 4);
                map.put(ArmorType.LEGGINGS, 7);
                map.put(ArmorType.CHESTPLATE, 9);
                map.put(ArmorType.HELMET, 4);
            }),
            20,
            SoundEvents.ARMOR_EQUIP_GENERIC,
            4.0F,
            0.2F,
            ModTags.Items.INGOTS_ENDERNIUM,
            ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath("endernium", "endernium"))
    );

}
