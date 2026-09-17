package com.skittlq.endernium;

import com.skittlq.endernium.client.ClientEvents;
import com.skittlq.endernium.client.vfx.EnderniumShaderRenderer;
import com.skittlq.endernium.client.vfx.EnderniumVfxCompatibility;
import com.skittlq.endernium.config.EnderniumConfigManager;
import com.skittlq.endernium.network.ModNetworking;
import com.skittlq.endernium.entity.ModEntities;
import com.skittlq.endernium.client.render.EnderniumThrownSpearRenderer;
import com.skittlq.endernium.particles.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class EnderniumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Endernium.LOGGER.info("Initializing Endernium client");
        EnderniumVfxCompatibility.bindPreference(() -> EnderniumConfigManager.getConfig().vfxRenderMode);
        EnderniumShaderRenderer.pipelines().forEach(RenderPipelines::register);
        ModParticles.registerClient();
        EntityRenderers.register(ModEntities.THROWN_SPEAR, EnderniumThrownSpearRenderer::new);
        ModNetworking.registerClient();
        ClientEvents.register();
    }
}
