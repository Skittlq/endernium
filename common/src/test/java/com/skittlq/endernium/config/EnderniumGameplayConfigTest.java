package com.skittlq.endernium.config;

import com.skittlq.endernium.client.EnderniumClientGameplaySettings;
import com.skittlq.endernium.util.EnderniumAbilityMath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnderniumGameplayConfigTest {
    @Test
    void swordChargeDerivesCompleteStrikesAndPartialProgress() {
        assertEquals(0, EnderniumAbilityMath.storedStrikes(23.99F, 24.0F, 15));
        assertEquals(1, EnderniumAbilityMath.storedStrikes(24.0F, 24.0F, 15));
        assertEquals(15, EnderniumAbilityMath.storedStrikes(1_000.0F, 24.0F, 15));
        assertEquals(0.5F, EnderniumAbilityMath.partialStrikeProgress(36.0F, 24.0F, 15), 0.0001F);
        assertEquals(1.0F, EnderniumAbilityMath.partialStrikeProgress(360.0F, 24.0F, 15), 0.0001F);
        assertEquals(360.0F, EnderniumAbilityMath.accumulateSwordCharge(350.0F, 50.0F,
                24.0F, 15), 0.0001F);
        assertEquals(30.0F, EnderniumAbilityMath.chargeAfterBarrageAttempt(30.0F, false));
        assertEquals(0.0F, EnderniumAbilityMath.chargeAfterBarrageAttempt(30.0F, true));
    }

    @Test
    void swordChargeUsesActualHealthLostInsteadOfOverkill() {
        assertEquals(3.0F, EnderniumAbilityMath.actualHealthDamage(3.0F, 0.0F, 20.0F));
        assertEquals(0.0F, EnderniumAbilityMath.actualHealthDamage(20.0F, 20.0F, 5.0F));
    }

    @Test
    void swordBarrageUsesOneFlatMultiplierForEveryTarget() {
        assertEquals(18.0F, EnderniumAbilityMath.swordBarrageDamage(9.0F));
        assertEquals(24.0F, EnderniumAbilityMath.swordBarrageDamage(12.0F));
    }

    @Test
    void armorStrengthIsLinearAndClamped() {
        assertEquals(0.0F, EnderniumAbilityMath.armorStrength(0.0F, 24.0F));
        assertEquals(0.25F, EnderniumAbilityMath.armorStrength(6.0F, 24.0F));
        assertEquals(0.5F, EnderniumAbilityMath.armorStrength(12.0F, 24.0F));
        assertEquals(0.75F, EnderniumAbilityMath.armorStrength(18.0F, 24.0F));
        assertEquals(1.0F, EnderniumAbilityMath.armorStrength(24.0F, 24.0F));
        assertEquals(1.0F, EnderniumAbilityMath.armorStrength(30.0F, 24.0F));
        assertEquals(24.0F, EnderniumAbilityMath.accumulateArmorCharge(18.0F, 20.0F, 24.0F));
    }

    @Test
    void spearStrainDamageFallsLinearlyToZero() {
        assertEquals(8.0F, EnderniumAbilityMath.spearStrainDamage(20.0F, 160L, 160, 0.4F), 0.0001F);
        assertEquals(6.0F, EnderniumAbilityMath.spearStrainDamage(20.0F, 120L, 160, 0.4F), 0.0001F);
        assertEquals(4.0F, EnderniumAbilityMath.spearStrainDamage(20.0F, 80L, 160, 0.4F), 0.0001F);
        assertEquals(2.0F, EnderniumAbilityMath.spearStrainDamage(20.0F, 40L, 160, 0.4F), 0.0001F);
        assertEquals(1.0F, EnderniumAbilityMath.spearStrainDamage(20.0F, 20L, 160, 0.4F), 0.0001F);
        assertEquals(0.0F, EnderniumAbilityMath.spearStrainDamage(20.0F, 0L, 160, 0.4F), 0.0001F);
        assertEquals(16.0F, EnderniumAbilityMath.spearStrainDamage(40.0F, 999L, 160, 0.4F), 0.0001F);
    }

    @Test
    void clientSafeDefaultsDoNotReadTheBoundLoaderConfig() {
        EnderniumGameplayConfig.bind(new EnderniumGameplayConfig.Settings() {
            private IllegalStateException unavailable() {
                return new IllegalStateException("config not loaded");
            }

            public boolean swordAbilityEnabled() { throw unavailable(); }
            public double swordDamagePerStrikeMultiplier() { throw unavailable(); }
            public int swordMaxStrikes() { throw unavailable(); }
            public boolean toolsVeinMiningEnabled() { throw unavailable(); }
            public boolean armorAbilityEnabled() { throw unavailable(); }
            public int armorAbilityThreshold() { throw unavailable(); }
            public double armorMaxStoredDamage() { throw unavailable(); }
            public int spearStrainDurationSeconds() { throw unavailable(); }
            public double spearMaximumHealthCostPercent() { throw unavailable(); }
        });

        EnderniumClientGameplaySettings.reset();
        EnderniumGameplayConfig.Snapshot defaults = EnderniumClientGameplaySettings.get();
        assertTrue(defaults.swordAbilityEnabled());
        assertEquals(24.0D / 9.0D, defaults.swordDamagePerStrikeMultiplier());
        assertEquals(15, defaults.swordMaxStrikes());
        assertEquals(4, defaults.armorThreshold());
        assertEquals(24.0D, defaults.armorMaxStoredDamage());
        assertEquals(8, defaults.spearStrainDurationSeconds());
        assertEquals(40.0D, defaults.spearMaximumHealthCostPercent());
    }
}
