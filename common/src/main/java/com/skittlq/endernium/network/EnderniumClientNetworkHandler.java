package com.skittlq.endernium.network;

import com.skittlq.endernium.client.CameraLerpHandler;
import com.skittlq.endernium.client.EnderniumClientBehavior;
import com.skittlq.endernium.client.EnderniumClientCooldowns;
import com.skittlq.endernium.client.EnderniumClientGameplaySettings;
import com.skittlq.endernium.client.EnderniumClientVeinMiningState;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager;
import com.skittlq.endernium.network.payloads.AbilityCooldownSyncPayload;
import com.skittlq.endernium.network.payloads.BlessingStatePayload;
import com.skittlq.endernium.network.payloads.BlessingVfxPayload;
import com.skittlq.endernium.network.payloads.CameraLerpPayload;
import com.skittlq.endernium.network.payloads.CombatOpponentsPayload;
import com.skittlq.endernium.network.payloads.DragonDeathVfxPayload;
import com.skittlq.endernium.network.payloads.GameplaySettingsPayload;
import com.skittlq.endernium.network.payloads.VeinMiningStatePayload;
import com.skittlq.endernium.progression.EnderniumBlessing;
import com.skittlq.endernium.util.EnderniumTargeting;

public final class EnderniumClientNetworkHandler {
    private EnderniumClientNetworkHandler() {
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

    public static void handleBlessingVfx(BlessingVfxPayload payload) {
        EnderniumVfxManager.onBlessingVfx(payload);
    }

    public static void handleAbilityCooldownSync(AbilityCooldownSyncPayload payload) {
        switch (payload.ability()) {
            case ARMOR -> EnderniumClientCooldowns.setArmorCooldown(
                    payload.endGameTime(), payload.durationTicks());
            case SWORD -> EnderniumClientCooldowns.setSwordCooldown(
                    payload.endGameTime(), payload.durationTicks());
            case HORSE -> EnderniumClientCooldowns.setHorseCooldown(
                    payload.endGameTime(), payload.durationTicks());
            case SPEAR -> EnderniumClientCooldowns.setSpearCooldown(
                    payload.endGameTime(), payload.durationTicks());
            case NAUTILUS -> EnderniumClientCooldowns.setNautilusCooldown(
                    payload.endGameTime(), payload.durationTicks());
        }
    }

    public static void handleBlessingState(BlessingStatePayload payload) {
        EnderniumBlessing.setClientBlessed(payload.blessed());
        if (payload.playReadyEffect()) {
            EnderniumClientBehavior.triggerBlessingReadyHud();
        }
    }

    public static void handleGameplaySettings(GameplaySettingsPayload payload) {
        EnderniumClientGameplaySettings.apply(payload);
    }

    public static void handleVeinMiningState(VeinMiningStatePayload payload) {
        EnderniumClientVeinMiningState.setState(
                payload.active(),
                payload.blockEndGameTime(),
                payload.blockDurationTicks()
        );
    }
}
