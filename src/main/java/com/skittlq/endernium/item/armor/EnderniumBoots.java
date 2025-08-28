package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.function.Consumer;

import static com.skittlq.endernium.item.armor.ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL;

public class EnderniumBoots extends Item {
    public EnderniumBoots(Properties properties) {
        super(properties.humanoidArmor(ENDERNIUM_ARMOR_MATERIAL, ArmorType.BOOTS).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipAdder, TooltipFlag flag) {
        tooltipAdder.accept(Component.literal("§5Ender Repulsion Ability"));
        tooltipAdder.accept(Component.literal("§5Triggers when your health is below " +
                Config.ENDERNIUM_ARMOR_ABILITY_THRESHOLD.getAsInt() + " HP and you have the full armor set equipped."));
        tooltipAdder.accept(Component.literal("§5Cooldown: " +
                Config.ENDERNIUM_ARMOR_ABILITY_COOLDOWN.getAsLong() + " seconds."));
        tooltipAdder.accept(Component.literal("§7Pushes nearby hostile mobs away and grants regeneration."));

        super.appendHoverText(stack, context, display, tooltipAdder, flag);
    }
}
