package com.skittlq.endernium.network;

import com.skittlq.endernium.client.CameraLerpHandler;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager;
import com.skittlq.endernium.network.payloads.CameraLerpPayload;
import com.skittlq.endernium.network.payloads.CombatOpponentsPayload;
import com.skittlq.endernium.network.payloads.DragonDeathVfxPayload;
import com.skittlq.endernium.util.EnderniumTargeting;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class ClientModNetworking {
    private ClientModNetworking() {
    }

    public static void handleCameraLerp(CameraLerpPayload payload) {
        CameraLerpHandler.onCameraLerpPacket(payload);
    }

    public static void handleCombatOpponents(CombatOpponentsPayload payload) {
        EnderniumTargeting.replaceClientCombatOpponents(payload.opponentIds());
    }

    public static void handleDragonDeathVfx(DragonDeathVfxPayload payload) {
        EnderniumVfxManager.onDragonDeathVfx(payload);
    }

    public static void sendAbilityActivation() {
        ClientPacketDistributor.sendToServer(com.skittlq.endernium.network.payloads.EnderniumAbilityPayload.INSTANCE);
    }
}