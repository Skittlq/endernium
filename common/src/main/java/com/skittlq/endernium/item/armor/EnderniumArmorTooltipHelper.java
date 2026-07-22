package com.skittlq.endernium.item.armor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

final class EnderniumArmorTooltipHelper {
    private static final int DEFAULT_THRESHOLD = 4;
    private static final long DEFAULT_COOLDOWN = 120L;

    private EnderniumArmorTooltipHelper() {
    }

    static void appendFullSetAbilityTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder) {
        var player = EnderniumArmorUtil.getTooltipPlayer(context);
        if (player == null || !EnderniumArmorUtil.hasFullEnderniumSet(player)) {
            return;
        }

        tooltipAdder.accept(Component.literal("\u00A75Ender Repulsion Ability"));
        tooltipAdder.accept(Component.literal("\u00A75Triggers when your health is below "
                + getArmorAbilityThreshold() + " HP and you have the full armor set equipped."));
        tooltipAdder.accept(Component.literal("\u00A75Cooldown: "
                + getArmorAbilityCooldown() + " seconds."));
        tooltipAdder.accept(Component.literal("\u00A77Pushes nearby hostile mobs away and grants regeneration."));
    }

    private static int getArmorAbilityThreshold() {
        Integer fabricValue = readFabricInt("enderniumArmorAbilityThreshold");
        if (fabricValue != null) {
            return fabricValue;
        }

        Integer neoForgeValue = readNeoForgeInt("ENDERNIUM_ARMOR_ABILITY_THRESHOLD", "getAsInt");
        return neoForgeValue != null ? neoForgeValue : DEFAULT_THRESHOLD;
    }

    private static long getArmorAbilityCooldown() {
        Long fabricValue = readFabricLong("enderniumArmorAbilityCooldown");
        if (fabricValue != null) {
            return fabricValue;
        }

        Long neoForgeValue = readNeoForgeLong("ENDERNIUM_ARMOR_ABILITY_COOLDOWN", "getAsLong");
        return neoForgeValue != null ? neoForgeValue : DEFAULT_COOLDOWN;
    }

    private static Integer readFabricInt(String fieldName) {
        try {
            Class<?> managerClass = Class.forName("com.skittlq.endernium.config.EnderniumConfigManager");
            Method getConfig = managerClass.getMethod("getConfig");
            Object config = getConfig.invoke(null);
            Field field = config.getClass().getField(fieldName);
            return field.getInt(config);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static Long readFabricLong(String fieldName) {
        try {
            Class<?> managerClass = Class.forName("com.skittlq.endernium.config.EnderniumConfigManager");
            Method getConfig = managerClass.getMethod("getConfig");
            Object config = getConfig.invoke(null);
            Field field = config.getClass().getField(fieldName);
            return field.getLong(config);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static Integer readNeoForgeInt(String fieldName, String methodName) {
        try {
            Class<?> configClass = Class.forName("com.skittlq.endernium.Config");
            Object value = configClass.getField(fieldName).get(null);
            Method getter = value.getClass().getMethod(methodName);
            return (Integer) getter.invoke(value);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static Long readNeoForgeLong(String fieldName, String methodName) {
        try {
            Class<?> configClass = Class.forName("com.skittlq.endernium.Config");
            Object value = configClass.getField(fieldName).get(null);
            Method getter = value.getClass().getMethod(methodName);
            return (Long) getter.invoke(value);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }
}