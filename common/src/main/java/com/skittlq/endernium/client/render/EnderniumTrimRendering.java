package com.skittlq.endernium.client.render;

import com.skittlq.endernium.item.armor.EnderniumBoots;
import com.skittlq.endernium.item.armor.EnderniumChestplate;
import com.skittlq.endernium.item.armor.EnderniumHelmet;
import com.skittlq.endernium.item.armor.EnderniumLeggings;
import com.skittlq.endernium.trim.ModTrimMaterials;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import java.util.Optional;

public final class EnderniumTrimRendering {
    private EnderniumTrimRendering() {
    }

    public static boolean shouldHandle(ItemStack stack, ArmorTrim trim) {
        return isEnderniumArmor(stack.getItem()) || isEnderniumTrim(trim);
    }

    public static Optional<String> suffix(ItemStack stack, ArmorTrim trim, Equippable equippable) {
        if (equippable.assetId().isEmpty()) {
            return Optional.empty();
        }

        String palettePath = trim.material().value().paletteId().getPath();
        String suffix = palettePath.substring(palettePath.lastIndexOf('/') + 1);
        if (isEnderniumArmor(stack.getItem()) && isEnderniumTrim(trim)) {
            suffix += "_darker";
        }
        return Optional.of(suffix);
    }

    private static boolean isEnderniumArmor(Item item) {
        return item instanceof EnderniumHelmet
                || item instanceof EnderniumChestplate
                || item instanceof EnderniumLeggings
                || item instanceof EnderniumBoots;
    }

    private static boolean isEnderniumTrim(ArmorTrim trim) {
        Identifier paletteId = trim.material().value().paletteId();
        return trim.material().unwrapKey().map(ModTrimMaterials.ENDERNIUM::equals).orElse(false)
                || paletteId.equals(ModTrimMaterials.ENDERNIUM_PALETTE);
    }
}
