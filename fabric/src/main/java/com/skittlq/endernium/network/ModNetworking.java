package com.skittlq.endernium.network;

import com.skittlq.endernium.client.CameraLerpHandler;
import com.skittlq.endernium.client.EnderniumClientCooldowns;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager;
import com.skittlq.endernium.item.EnderniumAbilityHandler;
import com.skittlq.endernium.network.payloads.AbilityCooldownSyncPayload;
import com.skittlq.endernium.network.payloads.CombatOpponentsPayload;
import com.skittlq.endernium.network.payloads.CameraLerpPayload;
import com.skittlq.endernium.network.payloads.EnderniumAbilityPayload;
import com.skittlq.endernium.network.payloads.DragonDeathVfxPayload;
import com.skittlq.endernium.util.EnderniumTargeting;
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
        PayloadTypeRegistry.clientboundPlay().register(AbilityCooldownSyncPayload.TYPE, AbilityCooldownSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(EnderniumAbilityPayload.TYPE, EnderniumAbilityPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(EnderniumAbilityPayload.TYPE,
                (payload, context) -> context.server().execute(() ->
                        EnderniumAbilityHandler.activateHeldAbility(context.player().level(), context.player())));
        EnderniumNetworking.bindCameraLerpSender((player, targetYaw, targetPitch, durationTicks) ->
                ServerPlayNetworking.send(player, new CameraLerpPayload(targetYaw, targetPitch, durationTicks)));
        EnderniumNetworking.bindCombatOpponentsSender((player, opponentIds) ->
                ServerPlayNetworking.send(player, new CombatOpponentsPayload(new ArrayList<>(opponentIds))));
        EnderniumNetworking.bindDragonDeathVfxSender(ServerPlayNetworking::send);
        EnderniumNetworking.bindAbilityCooldownSyncSender(ServerPlayNetworking::send);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(CameraLerpPayload.TYPE,
                (payload, context) -> context.client().execute(() -> CameraLerpHandler.onCameraLerpPacket(payload)));
        ClientPlayNetworking.registerGlobalReceiver(CombatOpponentsPayload.TYPE,
                (payload, context) -> context.client().execute(() ->
                        EnderniumTargeting.replaceClientCombatOpponents(payload.opponentIds())));
        ClientPlayNetworking.registerGlobalReceiver(DragonDeathVfxPayload.TYPE,
                (payload, context) -> context.client().execute(() ->
                        EnderniumVfxManager.onDragonDeathVfx(payload)));
        ClientPlayNetworking.registerGlobalReceiver(AbilityCooldownSyncPayload.TYPE,
                (payload, context) -> context.client().execute(() -> {
                    if (payload.ability() == AbilityCooldownSyncPayload.Ability.ARMOR) {
                        EnderniumClientCooldowns.setArmorCooldown(payload.endGameTime(), payload.durationTicks());
                    } else {
                        EnderniumClientCooldowns.setSwordCooldown(payload.endGameTime(), payload.durationTicks());
                    }
                }));
    }

    public static void sendCameraLerp(ServerPlayer player, float targetYaw, float targetPitch, int durationTicks) {
        EnderniumNetworking.sendCameraLerp(player, targetYaw, targetPitch, durationTicks);
    }

    public static void sendAbilityActivation() {
        ClientPlayNetworking.send(EnderniumAbilityPayload.INSTANCE);
    }
}
