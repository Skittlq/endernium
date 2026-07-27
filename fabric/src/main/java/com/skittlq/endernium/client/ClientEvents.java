package com.skittlq.endernium.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.config.EnderniumConfigManager;
import com.skittlq.endernium.network.ModNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class ClientEvents {
    private static final KeyMapping.Category ENDERNIUM_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "endernium")
    );
    private static final Identifier ARMOR_COOLDOWN_HUD = Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "armor_cooldown_hud");
    private static final KeyMapping ENDERNIUM_ABILITY_KEY = new KeyMapping(
            "key.endernium.activate_ability",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            ENDERNIUM_CATEGORY
    );
    private static boolean registered;

    private ClientEvents() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        KeyMappingHelper.registerKeyMapping(ENDERNIUM_ABILITY_KEY);
        EnderniumKeyBindings.bindAbilityKeyName(ENDERNIUM_ABILITY_KEY::getTranslatedKeyMessage);
        ClientTickEvents.END_CLIENT_TICK.register(EnderniumClientBehavior::tickClient);
        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::handleAbilityKey);
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, ARMOR_COOLDOWN_HUD, ClientEvents::renderArmorCooldownHud);
    }

    private static void handleAbilityKey(Minecraft client) {
        while (ENDERNIUM_ABILITY_KEY.consumeClick()) {
            if (client.player != null && client.getConnection() != null) {
                ModNetworking.sendAbilityActivation();
            }
        }
    }

    private static void renderArmorCooldownHud(GuiGraphicsExtractor gui, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (!EnderniumClientBehavior.shouldRenderArmorCooldown(
                player,
                EnderniumConfigManager.getConfig().enderniumArmorAbility
        )) {
            return;
        }

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        float cooldownRemaining = player.getCooldowns().getCooldownPercent(chestStack, 0.0F);
        if (cooldownRemaining <= 0.0F) {
            return;
        }

        float cooldownProgress = 1.0F - cooldownRemaining;
        EnderniumClientBehavior.HudIconPosition position = EnderniumClientBehavior.armorCooldownPosition(
                gui.guiWidth(),
                gui.guiHeight()
        );

        gui.blit(RenderPipelines.GUI_TEXTURED,
                EnderniumClientBehavior.ARMOR_COOLDOWN_ICON,
                position.x(),
                position.y(),
                0.0F,
                0.0F,
                EnderniumClientBehavior.ARMOR_ICON_SIZE,
                EnderniumClientBehavior.ARMOR_ICON_SIZE,
                EnderniumClientBehavior.ARMOR_ICON_SIZE,
                EnderniumClientBehavior.ARMOR_ICON_SIZE,
                EnderniumClientBehavior.ARMOR_COOLDOWN_BACKGROUND_COLOR);

        int fillHeight = EnderniumClientBehavior.cooldownFillHeight(cooldownProgress);
        if (fillHeight > 0) {
            int sourceY = EnderniumClientBehavior.ARMOR_ICON_SIZE - fillHeight;
            gui.blit(RenderPipelines.GUI_TEXTURED,
                    EnderniumClientBehavior.ARMOR_COOLDOWN_ICON,
                    position.x(),
                    position.y() + sourceY,
                    0.0F,
                    sourceY,
                    EnderniumClientBehavior.ARMOR_ICON_SIZE,
                    fillHeight,
                    EnderniumClientBehavior.ARMOR_ICON_SIZE,
                    EnderniumClientBehavior.ARMOR_ICON_SIZE,
                    EnderniumClientBehavior.ARMOR_COOLDOWN_FOREGROUND_COLOR);
        }
    }
}