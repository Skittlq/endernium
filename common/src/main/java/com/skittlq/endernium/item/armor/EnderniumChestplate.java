package com.skittlq.endernium.item.armor;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

public class EnderniumChestplate extends Item {
    public EnderniumChestplate(Properties properties) {
        super(properties.humanoidArmor(ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL, ArmorType.CHESTPLATE).fireResistant());
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (slot == EquipmentSlot.CHEST) {
            EnderniumArmorAbility.tickEquippedMob(entity);
        }
        super.inventoryTick(stack, level, entity, slot);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipAdder, TooltipFlag flag) {
        EnderniumArmorTooltipHelper.appendFullSetAbilityTooltip(tooltipAdder);
        super.appendHoverText(stack, context, display, tooltipAdder, flag);
    }
}
