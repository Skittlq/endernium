package com.skittlq.endernium.network;

import com.skittlq.endernium.network.payloads.DragonDeathVfxPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public final class EnderniumNetworking {
    private static CameraLerpSender cameraLerpSender = (player, targetYaw, targetPitch, durationTicks) -> {
        throw new IllegalStateException("Endernium camera lerp sender has not been bound to a loader network API yet");
    };
    private static CombatOpponentsSender combatOpponentsSender = (player, opponentIds) -> {
        throw new IllegalStateException("Endernium combat opponent sender has not been bound to a loader network API yet");
    };
    private static DragonDeathVfxSender dragonDeathVfxSender = (player, payload) -> {
        throw new IllegalStateException("Endernium dragon VFX sender has not been bound to a loader network API yet");
    };

    private EnderniumNetworking() {
    }

    public static void bindCameraLerpSender(CameraLerpSender sender) {
        cameraLerpSender = Objects.requireNonNull(sender);
    }

    public static void sendCameraLerp(ServerPlayer player, float targetYaw, float targetPitch, int durationTicks) {
        cameraLerpSender.send(player, targetYaw, targetPitch, durationTicks);
    }

    public static void bindCombatOpponentsSender(CombatOpponentsSender sender) {
        combatOpponentsSender = Objects.requireNonNull(sender);
    }

    public static void sendCombatOpponents(ServerPlayer player, Collection<UUID> opponentIds) {
        combatOpponentsSender.send(player, opponentIds);
    }

    public static void bindDragonDeathVfxSender(DragonDeathVfxSender sender) {
        dragonDeathVfxSender = Objects.requireNonNull(sender);
    }

    public static void sendDragonDeathVfx(ServerPlayer player, DragonDeathVfxPayload payload) {
        dragonDeathVfxSender.send(player, payload);
    }

    @FunctionalInterface
    public interface CameraLerpSender {
        void send(ServerPlayer player, float targetYaw, float targetPitch, int durationTicks);
    }

    @FunctionalInterface
    public interface CombatOpponentsSender {
        void send(ServerPlayer player, Collection<UUID> opponentIds);
    }

    @FunctionalInterface
    public interface DragonDeathVfxSender {
        void send(ServerPlayer player, DragonDeathVfxPayload payload);
    }
}
