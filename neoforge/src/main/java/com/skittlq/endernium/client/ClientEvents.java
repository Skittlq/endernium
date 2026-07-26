package com.skittlq.endernium.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.skittlq.endernium.Config;
import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.network.ModNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {
    private static final KeyMapping.Category ENDERNIUM_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(Endernium.MODID, "endernium")
    );
    private static final KeyMapping ENDERNIUM_ABILITY_KEY = new KeyMapping(
            "key.endernium.activate_ability",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            ENDERNIUM_CATEGORY
    );

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        EnderniumClientBehavior.tickClient(client);
        handleAbilityKey(client);
    }

    private static void handleAbilityKey(Minecraft client) {
        while (ENDERNIUM_ABILITY_KEY.consumeClick()) {
            if (client.player != null && client.getConnection() != null) {
                ModNetworking.sendAbilityActivation();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (!EnderniumClientBehavior.shouldRenderArmorCooldown(
                player,
                Config.ENDERNIUM_ARMOR_ABILITY.getAsBoolean()
        )) {
            return;
        }

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        float cooldownRemaining = player.getCooldowns().getCooldownPercent(chestStack, 0.0F);
        if (cooldownRemaining <= 0.0F) {
            return;
        }

        float cooldownProgress = 1.0F - cooldownRemaining;
        var gui = event.getGuiGraphics();
        EnderniumClientBehavior.HudIconPosition position = EnderniumClientBehavior.armorCooldownPosition(
                gui.guiWidth(),
                gui.guiHeight()
        );

        gui.blit(
                RenderPipelines.GUI_TEXTURED,
                EnderniumClientBehavior.ARMOR_COOLDOWN_ICON,
                position.x(),
                position.y(),
                0,
                0,
                EnderniumClientBehavior.ARMOR_ICON_SIZE,
                EnderniumClientBehavior.ARMOR_ICON_SIZE,
                EnderniumClientBehavior.ARMOR_ICON_SIZE,
                EnderniumClientBehavior.ARMOR_ICON_SIZE,
                EnderniumClientBehavior.ARMOR_COOLDOWN_BACKGROUND_COLOR
        );

        int fillHeight = EnderniumClientBehavior.cooldownFillHeight(cooldownProgress);
        if (fillHeight > 0) {
            int sourceY = EnderniumClientBehavior.ARMOR_ICON_SIZE - fillHeight;
            gui.blit(
                    RenderPipelines.GUI_TEXTURED,
                    EnderniumClientBehavior.ARMOR_COOLDOWN_ICON,
                    position.x(),
                    position.y() + sourceY,
                    0,
                    sourceY,
                    EnderniumClientBehavior.ARMOR_ICON_SIZE,
                    fillHeight,
                    EnderniumClientBehavior.ARMOR_ICON_SIZE,
                    EnderniumClientBehavior.ARMOR_ICON_SIZE,
                    EnderniumClientBehavior.ARMOR_COOLDOWN_FOREGROUND_COLOR
            );
        }
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ENDERNIUM_ABILITY_KEY);
    }
}