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
            EnderniumClientBehavior.resetArmorCooldownHud();
            return;
        }

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        float cooldownRemaining = player.getCooldowns().getCooldownPercent(chestStack, 0.0F);
        EnderniumClientBehavior.ArmorCooldownHudFrame frame =
                EnderniumClientBehavior.armorCooldownHudFrame(cooldownRemaining);
        if (!frame.visible()) {
            return;
        }
        if (frame.playReadySound()) {
            EnderniumClientBehavior.playArmorCooldownReadySound(mc);
        }

        var gui = event.getGuiGraphics();
        EnderniumClientBehavior.HudIconPosition position = EnderniumClientBehavior.armorCooldownPosition(
                gui.guiWidth(),
                gui.guiHeight(),
                player.getMainArm()
        );
        int iconX = position.x() + EnderniumClientBehavior.ARMOR_SPRITE_OFFSET_X;
        int iconY = position.y() + EnderniumClientBehavior.ARMOR_SPRITE_OFFSET_Y;
        int backgroundColor = EnderniumClientBehavior.hudColorWithOpacity(
                EnderniumClientBehavior.ARMOR_COOLDOWN_BACKGROUND_COLOR,
                frame.opacity()
        );
        int foregroundColor = EnderniumClientBehavior.hudColorWithOpacity(
                EnderniumClientBehavior.ARMOR_COOLDOWN_FOREGROUND_COLOR,
                frame.opacity()
        );

        gui.blit(
                RenderPipelines.GUI_TEXTURED,
                EnderniumClientBehavior.ARMOR_COOLDOWN_ICON,
                iconX,
                iconY,
                0,
                0,
                EnderniumClientBehavior.ARMOR_SPRITE_WIDTH,
                EnderniumClientBehavior.ARMOR_SPRITE_HEIGHT,
                EnderniumClientBehavior.ARMOR_SPRITE_WIDTH,
                EnderniumClientBehavior.ARMOR_SPRITE_HEIGHT,
                backgroundColor
        );

        int fillHeight = EnderniumClientBehavior.cooldownFillHeight(frame.cooldownProgress());
        if (fillHeight > 0) {
            int sourceY = EnderniumClientBehavior.ARMOR_SPRITE_HEIGHT - fillHeight;
            gui.blit(
                    RenderPipelines.GUI_TEXTURED,
                    EnderniumClientBehavior.ARMOR_COOLDOWN_ICON,
                    iconX,
                    iconY + sourceY,
                    0,
                    sourceY,
                    EnderniumClientBehavior.ARMOR_SPRITE_WIDTH,
                    fillHeight,
                    EnderniumClientBehavior.ARMOR_SPRITE_WIDTH,
                    EnderniumClientBehavior.ARMOR_SPRITE_HEIGHT,
                    foregroundColor
            );
        }

        int flashColor = EnderniumClientBehavior.hudColorWithOpacity(-1, frame.flashOpacity());
        if (frame.flashOpacity() > 0.0F) {
            gui.blit(
                    RenderPipelines.GUI_TEXTURED,
                    EnderniumClientBehavior.ARMOR_COOLDOWN_READY_ICON,
                    iconX,
                    iconY,
                    0,
                    0,
                    EnderniumClientBehavior.ARMOR_SPRITE_WIDTH,
                    EnderniumClientBehavior.ARMOR_SPRITE_HEIGHT,
                    EnderniumClientBehavior.ARMOR_SPRITE_WIDTH,
                    EnderniumClientBehavior.ARMOR_SPRITE_HEIGHT,
                    flashColor
            );
        }
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ENDERNIUM_ABILITY_KEY);
        EnderniumKeyBindings.bindAbilityKeyName(ENDERNIUM_ABILITY_KEY::getTranslatedKeyMessage);
    }
}
