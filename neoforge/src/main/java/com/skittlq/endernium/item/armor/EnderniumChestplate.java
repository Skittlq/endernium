package com.skittlq.endernium.item.armor;

import com.mojang.logging.LogUtils;
import com.skittlq.endernium.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.function.Consumer;

import static com.skittlq.endernium.item.armor.ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL;

public class EnderniumChestplate extends Item {
    public EnderniumChestplate(Properties properties) {
        super(properties.humanoidArmor(ENDERNIUM_ARMOR_MATERIAL, ArmorType.CHESTPLATE).fireResistant());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipAdder, TooltipFlag flag) {
        var player = EnderniumArmorUtil.getTooltipPlayer(context);
        LogUtils.getLogger().info("Appending tooltip for Endernium Chestplate. Player present: " + (player != null));
        if (player != null && EnderniumArmorUtil.hasFullEnderniumSet(player)) {
            tooltipAdder.accept(Component.literal("§5Ender Repulsion Ability"));
            tooltipAdder.accept(Component.literal("§5Triggers when your health is below " +
                    Config.ENDERNIUM_ARMOR_ABILITY_THRESHOLD.getAsInt() + " HP and you have the full armor set equipped."));
            tooltipAdder.accept(Component.literal("§5Cooldown: " +
                    Config.ENDERNIUM_ARMOR_ABILITY_COOLDOWN.getAsLong() + " seconds."));
            tooltipAdder.accept(Component.literal("§7Pushes nearby hostile mobs away and grants regeneration."));
            tooltipAdder.accept(Component.literal(""));
        }

        super.appendHoverText(stack, context, display, tooltipAdder, flag);
    }
}
