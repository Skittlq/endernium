package com.skittlq.endernium.client;

import com.skittlq.endernium.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        EnderniumClientBehavior.tickClient(Minecraft.getInstance());
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
}