package com.skittlq.endernium.client;

import com.skittlq.endernium.config.EnderniumGameplayConfig;
import com.skittlq.endernium.network.payloads.GameplaySettingsPayload;

/** Client-only mirror of the connected server's authoritative gameplay settings. */
public final class EnderniumClientGameplaySettings {
    private static EnderniumGameplayConfig.Snapshot settings = EnderniumGameplayConfig.Snapshot.defaults();

    private EnderniumClientGameplaySettings() {
    }

    public static EnderniumGameplayConfig.Snapshot get() {
        return settings;
    }

    public static void apply(GameplaySettingsPayload payload) {
        settings = new EnderniumGameplayConfig.Snapshot(payload.swordEnabled(),
                payload.swordDamagePerStrikeMultiplier(), payload.swordMaxStrikes(),
                payload.veinMiningEnabled(), payload.armorEnabled(), payload.armorThreshold(),
                payload.armorMaxStoredDamage(), payload.spearStrainDurationSeconds(),
                payload.spearMaximumHealthCostPercent());
    }

    public static void reset() {
        settings = EnderniumGameplayConfig.Snapshot.defaults();
    }
}
