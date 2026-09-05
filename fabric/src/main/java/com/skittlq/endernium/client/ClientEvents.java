package com.skittlq.endernium.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.config.EnderniumConfigManager;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager;
import com.skittlq.endernium.client.vfx.EnderniumShaderRenderer;
import com.skittlq.endernium.network.ModNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.glfw.GLFW;

public final class ClientEvents {
    private static final KeyMapping.Category ENDERNIUM_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "endernium")
    );
    private static final Identifier COOLDOWN_HUD = Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "ability_cooldown_hud");
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
        LevelExtractionEvents.END_EXTRACTION.register(context -> EnderniumVfxManager.extract(Minecraft.getInstance()));
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(context -> EnderniumShaderRenderer.instance().render(
                context.levelState().cameraRenderState.viewRotationMatrix,
                Minecraft.getInstance().gameRenderer.mainCamera().position()));
        LevelRenderEvents.END_MAIN.register(context -> EnderniumShaderRenderer.instance().renderPost(
                Minecraft.getInstance().gameRenderer.mainCamera().position()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> EnderniumVfxManager.clear());
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            public Identifier getFabricId() {
                return Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "shader_framework_reload");
            }
            public void onResourceManagerReload(ResourceManager manager) {
                EnderniumVfxManager.clear();
                EnderniumShaderRenderer.instance().resetBuffers();
            }
        });
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, COOLDOWN_HUD, ClientEvents::renderCooldownHuds);
    }

    private static void handleAbilityKey(Minecraft client) {
        while (ENDERNIUM_ABILITY_KEY.consumeClick()) {
            if (client.player != null && client.getConnection() != null) {
                ModNetworking.sendAbilityActivation();
            }
        }
    }

    private static void renderCooldownHuds(GuiGraphicsExtractor gui, DeltaTracker deltaTracker) {
        EnderniumClientBehavior.renderCooldownHuds(
                Minecraft.getInstance(),
                gui,
                EnderniumConfigManager.getConfig().enderniumArmorAbility,
                EnderniumConfigManager.getConfig().enderniumSwordAbility
        );
    }
}

