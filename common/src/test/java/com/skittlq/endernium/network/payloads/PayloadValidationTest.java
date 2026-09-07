package com.skittlq.endernium.network.payloads;

import com.skittlq.endernium.config.EnderniumGameplayConfig;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayloadValidationTest {
    @Test
    void cameraValuesAreFiniteAndBounded() {
        CameraLerpPayload payload = new CameraLerpPayload(Float.NaN, Float.POSITIVE_INFINITY, Integer.MAX_VALUE);
        assertTrue(Float.isFinite(payload.targetYaw()));
        assertTrue(Float.isFinite(payload.targetPitch()));
        assertEquals(200, payload.durationTicks());
    }

    @Test
    void combatOpponentCollectionsAreBounded() {
        CombatOpponentsPayload payload = new CombatOpponentsPayload(
                Collections.nCopies(2_000, UUID.randomUUID()));
        assertEquals(1_024, payload.opponentIds().size());
    }

    @Test
    void gameplaySettingsAreNormalized() {
        GameplaySettingsPayload payload = new GameplaySettingsPayload(
                true, -4, Integer.MAX_VALUE, true, true, Integer.MAX_VALUE, Long.MAX_VALUE);
        assertEquals(0, payload.swordBaseCooldownSeconds());
        assertEquals(2_048, payload.armorThreshold());
        assertEquals(EnderniumGameplayConfig.MAX_COOLDOWN_SECONDS, payload.armorCooldownSeconds());
    }
}
