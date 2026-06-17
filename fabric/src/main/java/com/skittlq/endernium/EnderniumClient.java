package com.skittlq.endernium;

import com.skittlq.endernium.client.ClientEvents;
import com.skittlq.endernium.network.ModNetworking;
import com.skittlq.endernium.particles.ModParticles;
import net.fabricmc.api.ClientModInitializer;

public class EnderniumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Endernium.LOGGER.info("Initializing Endernium client");
        ModParticles.registerClient();
        ModNetworking.registerClient();
        ClientEvents.register();
    }
}

