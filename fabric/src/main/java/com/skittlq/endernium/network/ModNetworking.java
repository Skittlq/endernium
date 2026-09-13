package com.skittlq.endernium.network;

import com.skittlq.endernium.item.EnderniumAbilityHandler;
import com.skittlq.endernium.network.payloads.AbilityCooldownSyncPayload;
import com.skittlq.endernium.network.payloads.BlessingStatePayload;
import com.skittlq.endernium.network.payloads.BlessingVfxPayload;
import com.skittlq.endernium.network.payloads.CombatOpponentsPayload;
import com.skittlq.endernium.network.payloads.CameraLerpPayload;
import com.skittlq.endernium.network.payloads.EnderniumAbilityPayload;
import com.skittlq.endernium.network.payloads.DragonDeathVfxPayload;
import com.skittlq.endernium.network.payloads.GameplaySettingsPayload;
import com.skittlq.endernium.network.payloads.VeinMiningStatePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(CameraLerpPayload.TYPE, CameraLerpPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CombatOpponentsPayload.TYPE, CombatOpponentsPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(DragonDeathVfxPayload.TYPE, DragonDeathVfxPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BlessingVfxPayload.TYPE, BlessingVfxPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(AbilityCooldownSyncPayload.TYPE, AbilityCooldownSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(BlessingStatePayload.TYPE, BlessingStatePayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(GameplaySettingsPayload.TYPE, GameplaySettingsPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(VeinMiningStatePayload.TYPE, VeinMiningStatePayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(EnderniumAbilityPayload.TYPE, EnderniumAbilityPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(EnderniumAbilityPayload.TYPE,
                (payload, context) -> context.server().execute(() ->
                        EnderniumAbilityHandler.activateHeldAbility(context.player().level(), context.player())));
        EnderniumNetworking.bindCameraLerpSender((player, targetYaw, targetPitch, durationTicks) ->
                ServerPlayNetworking.send(player, new CameraLerpPayload(targetYaw, targetPitch, durationTicks)));
        EnderniumNetworking.bindCombatOpponentsSender((player, opponentIds) ->
                ServerPlayNetworking.send(player, new CombatOpponentsPayload(new ArrayList<>(opponentIds))));
        EnderniumNetworking.bindDragonDeathVfxSender(ServerPlayNetworking::send);
        EnderniumNetworking.bindBlessingVfxSender(ServerPlayNetworking::send);
        EnderniumNetworking.bindAbilityCooldownSyncSender(ServerPlayNetworking::send);
        EnderniumNetworking.bindBlessingStateSender(ServerPlayNetworking::send);
        EnderniumNetworking.bindGameplaySettingsSender(ServerPlayNetworking::send);
        EnderniumNetworking.bindVeinMiningStateSender(ServerPlayNetworking::send);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(CameraLerpPayload.TYPE,
                (payload, context) -> context.client().execute(() ->
                        EnderniumClientNetworkHandler.handleCameraLerp(payload)));
        ClientPlayNetworking.registerGlobalReceiver(CombatOpponentsPayload.TYPE,
                (payload, context) -> context.client().execute(() ->
                        EnderniumClientNetworkHandler.handleCombatOpponents(payload)));
        ClientPlayNetworking.registerGlobalReceiver(DragonDeathVfxPayload.TYPE,
                (payload, context) -> context.client().execute(() ->
                        EnderniumClientNetworkHandler.handleDragonDeathVfx(payload)));
        ClientPlayNetworking.registerGlobalReceiver(BlessingVfxPayload.TYPE,
                (payload, context) -> context.client().execute(() ->
                        EnderniumClientNetworkHandler.handleBlessingVfx(payload)));
        ClientPlayNetworking.registerGlobalReceiver(AbilityCooldownSyncPayload.TYPE,
                (payload, context) -> context.client().execute(() ->
                        EnderniumClientNetworkHandler.handleAbilityCooldownSync(payload)));
        ClientPlayNetworking.registerGlobalReceiver(BlessingStatePayload.TYPE,
                (payload, context) -> context.client().execute(() ->
                        EnderniumClientNetworkHandler.handleBlessingState(payload)));
        ClientPlayNetworking.registerGlobalReceiver(GameplaySettingsPayload.TYPE,
                (payload, context) -> context.client().execute(() ->
                        EnderniumClientNetworkHandler.handleGameplaySettings(payload)));
        ClientPlayNetworking.registerGlobalReceiver(VeinMiningStatePayload.TYPE,
                (payload, context) -> context.client().execute(() ->
                        EnderniumClientNetworkHandler.handleVeinMiningState(payload)));
    }

    public static void sendCameraLerp(ServerPlayer player, float targetYaw, float targetPitch, int durationTicks) {
        EnderniumNetworking.sendCameraLerp(player, targetYaw, targetPitch, durationTicks);
    }

    public static void sendAbilityActivation() {
        ClientPlayNetworking.send(EnderniumAbilityPayload.INSTANCE);
    }
}
