package com.skittlq.endernium;

import com.skittlq.endernium.client.ClientEvents;
import com.skittlq.endernium.client.vfx.EnderniumShaderRenderer;
import com.skittlq.endernium.config.EnderniumConfigManager;
import com.skittlq.endernium.config.EnderniumVisualConfig;
import com.skittlq.endernium.network.ModNetworking;
import com.skittlq.endernium.particles.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.RenderPipelines;

public class EnderniumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Endernium.LOGGER.info("Initializing Endernium client");
        EnderniumVisualConfig.bind(new EnderniumVisualConfig.Settings() {
            public EnderniumVisualConfig.EffectQuality quality() {
                return EnderniumConfigManager.getConfig().enderniumEffectQuality;
            }
        });
        EnderniumShaderRenderer.pipelines().forEach(RenderPipelines::register);
        ModParticles.registerClient();
        ModNetworking.registerClient();
        ClientEvents.register();
    }
}

