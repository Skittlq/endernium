package com.skittlq.endernium.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager;
import com.skittlq.endernium.client.vfx.EnderniumShaderRenderer;
import com.skittlq.endernium.network.payloads.EnderniumAbilityPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {
    private static final KeyMapping.Category ENDERNIUM_CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(Endernium.MODID, "endernium")
    );
    private static final KeyMapping ENDERNIUM_ABILITY_KEY = new KeyMapping(
            "key.endernium.activate_ability",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_R,
            ENDERNIUM_CATEGORY
    );

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        EnderniumClientBehavior.tickClient(client);
        EnderniumAbilityKeyHandler.tick(client, ENDERNIUM_ABILITY_KEY,
                () -> ClientPacketDistributor.sendToServer(EnderniumAbilityPayload.INSTANCE));
    }

    @SubscribeEvent
    public static void extractVfx(ExtractLevelRenderStateEvent event) {
        EnderniumVfxManager.extract(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void renderVfx(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        Minecraft client = Minecraft.getInstance();
        EnderniumShaderRenderer.instance().render(event.getModelViewMatrix(), client.gameRenderer.mainCamera().position());
    }

    @SubscribeEvent
    public static void renderVfxPost(RenderLevelStageEvent.AfterLevel event) {
        Minecraft client = Minecraft.getInstance();
        EnderniumShaderRenderer.instance().renderPost(client.gameRenderer.mainCamera().position());
    }

    @SubscribeEvent
    public static void clearVfx(ClientPlayerNetworkEvent.LoggingOut event) {
        EnderniumClientBehavior.resetSessionState();
    }

    @SubscribeEvent
    public static void closeRenderer(ClientStoppingEvent event) {
        EnderniumShaderRenderer.instance().close();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        EnderniumClientBehavior.renderCooldownHuds(
                Minecraft.getInstance(),
                event.getGuiGraphics(),
                EnderniumClientGameplaySettings.get().armorAbilityEnabled(),
                EnderniumClientGameplaySettings.get().swordAbilityEnabled()
        );
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(ENDERNIUM_CATEGORY);
        event.register(ENDERNIUM_ABILITY_KEY);
        EnderniumKeyBindings.bindAbilityKeyName(ENDERNIUM_ABILITY_KEY::getTranslatedKeyMessage);
        EnderniumKeyBindings.bindSneakKeyName(
                () -> Minecraft.getInstance().options.keyShift.getTranslatedKeyMessage());
    }
}
