package com.skittlq.endernium.item;

import com.skittlq.endernium.item.tools.EnderniumAxe;
import com.skittlq.endernium.item.tools.EnderniumHoe;
import com.skittlq.endernium.item.tools.EnderniumPickaxe;
import com.skittlq.endernium.item.tools.EnderniumShovel;
import com.skittlq.endernium.item.tools.EnderniumSword;
import com.skittlq.endernium.item.tools.EnderniumVeinMiningToolHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class EnderniumAbilityHandler {
    private EnderniumAbilityHandler() {
    }

    public static void activateHeldAbility(Level level, Player player) {
        if (tryActivate(level, player, InteractionHand.MAIN_HAND)) {
            return;
        }
        tryActivate(level, player, InteractionHand.OFF_HAND);
    }

    private static boolean tryActivate(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        if (item instanceof EnderniumSword sword) {
            sword.activateAbility(level, player, hand);
            return true;
        }

        if (item instanceof EnderniumPickaxe
                || item instanceof EnderniumShovel
                || item instanceof EnderniumAxe
                || item instanceof EnderniumHoe) {
            EnderniumVeinMiningToolHelper.activate(level, player, hand);
            return true;
        }

        return false;
    }
}
