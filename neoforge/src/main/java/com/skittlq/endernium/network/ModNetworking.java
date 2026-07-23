package com.skittlq.endernium.network;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.client.CameraLerpHandler;
import com.skittlq.endernium.network.payloads.CameraLerpPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Endernium.MODID)
public class ModNetworking {
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                CameraLerpPayload.TYPE,
                CameraLerpPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> CameraLerpHandler.onCameraLerpPacket(payload))
        );
        EnderniumNetworking.bindCameraLerpSender((player, targetYaw, targetPitch, durationTicks) ->
                PacketDistributor.sendToPlayer(player, new CameraLerpPayload(targetYaw, targetPitch, durationTicks)));
    }

    public static void sendCameraLerp(ServerPlayer player, float targetYaw, float targetPitch, int durationTicks) {
        EnderniumNetworking.sendCameraLerp(player, targetYaw, targetPitch, durationTicks);
    }
}