package com.skittlq.endernium.network;

import com.skittlq.endernium.client.CameraLerpHandler;
import com.skittlq.endernium.item.EnderniumAbilityHandler;
import com.skittlq.endernium.network.payloads.CameraLerpPayload;
import com.skittlq.endernium.network.payloads.EnderniumAbilityPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(CameraLerpPayload.TYPE, CameraLerpPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(EnderniumAbilityPayload.TYPE, EnderniumAbilityPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(EnderniumAbilityPayload.TYPE,
                (payload, context) -> context.server().execute(() ->
                        EnderniumAbilityHandler.activateHeldAbility(context.player().level(), context.player())));
        EnderniumNetworking.bindCameraLerpSender((player, targetYaw, targetPitch, durationTicks) ->
                ServerPlayNetworking.send(player, new CameraLerpPayload(targetYaw, targetPitch, durationTicks)));
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(CameraLerpPayload.TYPE,
                (payload, context) -> context.client().execute(() -> CameraLerpHandler.onCameraLerpPacket(payload)));
    }

    public static void sendCameraLerp(ServerPlayer player, float targetYaw, float targetPitch, int durationTicks) {
        EnderniumNetworking.sendCameraLerp(player, targetYaw, targetPitch, durationTicks);
    }

    public static void sendAbilityActivation() {
        ClientPlayNetworking.send(EnderniumAbilityPayload.INSTANCE);
    }
}