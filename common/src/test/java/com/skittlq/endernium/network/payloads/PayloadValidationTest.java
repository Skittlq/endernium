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
                true, -4.0D, Integer.MAX_VALUE, true, true, Integer.MAX_VALUE,
                Double.POSITIVE_INFINITY, Integer.MAX_VALUE, 200.0D);
        assertEquals(EnderniumGameplayConfig.MIN_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER,
                payload.swordDamagePerStrikeMultiplier());
        assertEquals(EnderniumGameplayConfig.MAX_SWORD_STRIKES, payload.swordMaxStrikes());
        assertEquals(2_048, payload.armorThreshold());
        assertEquals(EnderniumGameplayConfig.DEFAULT_ARMOR_MAX_STORED_DAMAGE,
                payload.armorMaxStoredDamage());
        assertEquals(EnderniumGameplayConfig.MAX_SPEAR_STRAIN_DURATION_SECONDS,
                payload.spearStrainDurationSeconds());
        assertEquals(100.0D, payload.spearMaximumHealthCostPercent());
    }

    @Test
    void chargePayloadsRejectNegativeAndNonFiniteValues() {
        assertEquals(0.0F, new SwordChargeSyncPayload(Float.NaN).storedDamage());
        assertEquals(0.0F, new SwordChargeSyncPayload(-1.0F).storedDamage());
        assertEquals(0.0F, new ArmorChargeSyncPayload(Float.POSITIVE_INFINITY).storedDamage());
        assertEquals(4.5F, new ArmorChargeSyncPayload(4.5F).storedDamage());
    }
}
