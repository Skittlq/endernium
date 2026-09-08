package com.skittlq.endernium.network;

import com.skittlq.endernium.client.CameraLerpHandler;
import com.skittlq.endernium.client.EnderniumClientBehavior;
import com.skittlq.endernium.client.EnderniumClientCooldowns;
import com.skittlq.endernium.client.EnderniumClientGameplaySettings;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager;
import com.skittlq.endernium.network.payloads.AbilityCooldownSyncPayload;
import com.skittlq.endernium.network.payloads.AwakeningStatePayload;
import com.skittlq.endernium.network.payloads.BlessingVfxPayload;
import com.skittlq.endernium.network.payloads.CameraLerpPayload;
import com.skittlq.endernium.network.payloads.CombatOpponentsPayload;
import com.skittlq.endernium.network.payloads.DragonDeathVfxPayload;
import com.skittlq.endernium.network.payloads.GameplaySettingsPayload;
import com.skittlq.endernium.progression.EnderniumAwakening;
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
        if (payload.ability() == AbilityCooldownSyncPayload.Ability.ARMOR) {
            EnderniumClientCooldowns.setArmorCooldown(payload.endGameTime(), payload.durationTicks());
        } else {
            EnderniumClientCooldowns.setSwordCooldown(payload.endGameTime(), payload.durationTicks());
        }
    }

    public static void handleAwakeningState(AwakeningStatePayload payload) {
        EnderniumAwakening.setClientAwakened(payload.awakened());
        if (payload.playReadyEffect()) {
            EnderniumClientBehavior.triggerAwakeningReadyHud();
        }
    }

    public static void handleGameplaySettings(GameplaySettingsPayload payload) {
        EnderniumClientGameplaySettings.apply(payload);
    }
}
