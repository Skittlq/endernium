package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnderniumBoots extends ArmorItem {
    public EnderniumBoots(ArmorMaterial pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        components.add(Component.literal("§5Ender Repulsion Ability"));
        components.add(Component.literal("§5Triggers when your health is below " +
                Config.ENDERNIUM_ARMOR_ABILITY_THRESHOLD.get() + " HP and you have the full armor set equipped."));
        components.add(Component.literal("§5Cooldown: " +
                Config.ENDERNIUM_ARMOR_ABILITY_COOLDOWN.get() + " seconds."));
        components.add(Component.literal("§7Pushes nearby hostile mobs away and grants regeneration."));

        super.appendHoverText(stack, level, components, tooltipFlag);
    }
}
