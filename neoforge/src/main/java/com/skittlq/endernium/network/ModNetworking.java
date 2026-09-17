package com.skittlq.endernium.network;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.item.EnderniumAbilityHandler;
import com.skittlq.endernium.network.payloads.AbilityCooldownSyncPayload;
import com.skittlq.endernium.network.payloads.BlessingStatePayload;
import com.skittlq.endernium.network.payloads.BlessingVfxPayload;
import com.skittlq.endernium.network.payloads.CameraLerpPayload;
import com.skittlq.endernium.network.payloads.CombatOpponentsPayload;
import com.skittlq.endernium.network.payloads.EnderniumAbilityPayload;
import com.skittlq.endernium.network.payloads.DragonDeathVfxPayload;
import com.skittlq.endernium.network.payloads.GameplaySettingsPayload;
import com.skittlq.endernium.network.payloads.VeinMiningStatePayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;

@EventBusSubscriber(modid = Endernium.MODID)
public class ModNetworking {
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                CameraLerpPayload.TYPE,
                CameraLerpPayload.STREAM_CODEC,
                                (payload, context) -> context.enqueueWork(() -> {
                                            if (FMLLoader.getCurrent().getDist() == Dist.CLIENT) {
                                                EnderniumClientNetworkHandler.handleCameraLerp(payload);
                                        }
                                })
        );
        registrar.playToClient(
                CombatOpponentsPayload.TYPE,
                CombatOpponentsPayload.STREAM_CODEC,
                                (payload, context) -> context.enqueueWork(() -> {
                                            if (FMLLoader.getCurrent().getDist() == Dist.CLIENT) {
                                                EnderniumClientNetworkHandler.handleCombatOpponents(payload);
                                        }
                                })
        );
        registrar.playToClient(
                DragonDeathVfxPayload.TYPE,
                DragonDeathVfxPayload.STREAM_CODEC,
                                (payload, context) -> context.enqueueWork(() -> {
                                            if (FMLLoader.getCurrent().getDist() == Dist.CLIENT) {
                                                EnderniumClientNetworkHandler.handleDragonDeathVfx(payload);
                                        }
                                })
        );
        registrar.playToClient(
                BlessingVfxPayload.TYPE,
                BlessingVfxPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (FMLLoader.getCurrent().getDist() == Dist.CLIENT) {
                        EnderniumClientNetworkHandler.handleBlessingVfx(payload);
                    }
                })
        );
        registrar.playToClient(
                AbilityCooldownSyncPayload.TYPE,
                AbilityCooldownSyncPayload.STREAM_CODEC,
                                (payload, context) -> context.enqueueWork(() -> {
                                            if (FMLLoader.getCurrent().getDist() == Dist.CLIENT) {
                                                EnderniumClientNetworkHandler.handleAbilityCooldownSync(payload);
                                        }
                                })
        );
        registrar.playToClient(
                BlessingStatePayload.TYPE,
                BlessingStatePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (FMLLoader.getCurrent().getDist() == Dist.CLIENT) {
                        EnderniumClientNetworkHandler.handleBlessingState(payload);
                    }
                })
        );
        registrar.playToClient(
                GameplaySettingsPayload.TYPE,
                GameplaySettingsPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (FMLLoader.getCurrent().getDist() == Dist.CLIENT) {
                        EnderniumClientNetworkHandler.handleGameplaySettings(payload);
                    }
                })
        );
        registrar.playToClient(
                VeinMiningStatePayload.TYPE,
                VeinMiningStatePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (FMLLoader.getCurrent().getDist() == Dist.CLIENT) {
                        EnderniumClientNetworkHandler.handleVeinMiningState(payload);
                    }
                })
        );
        registrar.playToServer(
                EnderniumAbilityPayload.TYPE,
                EnderniumAbilityPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        EnderniumAbilityHandler.activateHeldAbility(context.player().level(), context.player()))
        );
        EnderniumNetworking.bindCameraLerpSender((player, targetYaw, targetPitch, durationTicks) ->
                PacketDistributor.sendToPlayer(player, new CameraLerpPayload(targetYaw, targetPitch, durationTicks)));
        EnderniumNetworking.bindCombatOpponentsSender((player, opponentIds) ->
                PacketDistributor.sendToPlayer(player,
                        new CombatOpponentsPayload(new ArrayList<>(opponentIds))));
        EnderniumNetworking.bindDragonDeathVfxSender(PacketDistributor::sendToPlayer);
        EnderniumNetworking.bindBlessingVfxSender(PacketDistributor::sendToPlayer);
        EnderniumNetworking.bindAbilityCooldownSyncSender(PacketDistributor::sendToPlayer);
        EnderniumNetworking.bindBlessingStateSender(PacketDistributor::sendToPlayer);
        EnderniumNetworking.bindGameplaySettingsSender(PacketDistributor::sendToPlayer);
        EnderniumNetworking.bindVeinMiningStateSender(PacketDistributor::sendToPlayer);
    }

    public static void sendCameraLerp(ServerPlayer player, float targetYaw, float targetPitch, int durationTicks) {
        EnderniumNetworking.sendCameraLerp(player, targetYaw, targetPitch, durationTicks);
    }

}
