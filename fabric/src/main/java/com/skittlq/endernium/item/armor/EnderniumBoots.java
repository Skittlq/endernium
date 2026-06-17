package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.config.EnderniumConfigManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.function.Consumer;

public class EnderniumBoots extends Item {
    public EnderniumBoots(Properties properties) {
        super(properties.humanoidArmor(ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL, ArmorType.BOOTS).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipAdder, TooltipFlag flag) {
        var player = EnderniumArmorUtil.getTooltipPlayer(context);
        var config = EnderniumConfigManager.getConfig();
        if (player != null && EnderniumArmorUtil.hasFullEnderniumSet(player)) {
            tooltipAdder.accept(Component.literal("§5Ender Repulsion Ability"));
            tooltipAdder.accept(Component.literal("§5Triggers when your health is below "
                    + config.enderniumArmorAbilityThreshold + " HP and you have the full armor set equipped."));
            tooltipAdder.accept(Component.literal("§5Cooldown: "
                    + config.enderniumArmorAbilityCooldown + " seconds."));
            tooltipAdder.accept(Component.literal("§7Pushes nearby hostile mobs away and grants regeneration."));
            tooltipAdder.accept(Component.literal(""));
        }

        super.appendHoverText(stack, context, display, tooltipAdder, flag);
    }
}

