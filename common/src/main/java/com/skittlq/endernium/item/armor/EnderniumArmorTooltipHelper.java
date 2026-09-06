package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.progression.EnderniumAwakening;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

final class EnderniumArmorTooltipHelper {
    private static final int DEFAULT_THRESHOLD = 4;
    private static final long DEFAULT_COOLDOWN = 90L;

    private EnderniumArmorTooltipHelper() {
    }

    static void appendFullSetAbilityTooltip(Consumer<Component> tooltipAdder) {
        if (!isArmorAbilityEnabled()) {
            return;
        }

        if (!EnderniumAwakening.isClientAwakened()) {
            tooltipAdder.accept(Component.translatable("endernium.tooltip.ability.locked")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        tooltipAdder.accept(Component.translatable("endernium.tooltip.armor_ability.title").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipAdder.accept(Component.translatable(
                "endernium.tooltip.armor_ability.trigger",
                getArmorAbilityThreshold()
        ).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipAdder.accept(Component.translatable(
                "endernium.tooltip.armor_ability.cooldown",
                getArmorAbilityCooldown()
        ).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipAdder.accept(Component.translatable("endernium.tooltip.armor_ability.description").withStyle(ChatFormatting.GRAY));
    }

    private static boolean isArmorAbilityEnabled() {
        Boolean fabricValue = readFabricBoolean("enderniumArmorAbility");
        if (fabricValue != null) {
            return fabricValue;
        }

        Boolean neoForgeValue = readNeoForgeBoolean("ENDERNIUM_ARMOR_ABILITY", "getAsBoolean");
        return neoForgeValue == null || neoForgeValue;
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

    private static Boolean readFabricBoolean(String fieldName) {
        try {
            Class<?> managerClass = Class.forName("com.skittlq.endernium.config.EnderniumConfigManager");
            Method getConfig = managerClass.getMethod("getConfig");
            Object config = getConfig.invoke(null);
            Field field = config.getClass().getField(fieldName);
            return field.getBoolean(config);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
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

    private static Boolean readNeoForgeBoolean(String fieldName, String methodName) {
        try {
            Class<?> configClass = Class.forName("com.skittlq.endernium.Config");
            Object value = configClass.getField(fieldName).get(null);
            Method getter = value.getClass().getMethod(methodName);
            return (Boolean) getter.invoke(value);
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
