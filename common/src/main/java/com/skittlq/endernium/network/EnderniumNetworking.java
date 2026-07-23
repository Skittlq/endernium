package com.skittlq.endernium.network;

import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public final class EnderniumNetworking {
    private static CameraLerpSender cameraLerpSender = (player, targetYaw, targetPitch, durationTicks) -> {
        throw new IllegalStateException("Endernium camera lerp sender has not been bound to a loader network API yet");
    };

    private EnderniumNetworking() {
    }

    public static void bindCameraLerpSender(CameraLerpSender sender) {
        cameraLerpSender = Objects.requireNonNull(sender);
    }

    public static void sendCameraLerp(ServerPlayer player, float targetYaw, float targetPitch, int durationTicks) {
        cameraLerpSender.send(player, targetYaw, targetPitch, durationTicks);
    }

    @FunctionalInterface
    public interface CameraLerpSender {
        void send(ServerPlayer player, float targetYaw, float targetPitch, int durationTicks);
    }
}
