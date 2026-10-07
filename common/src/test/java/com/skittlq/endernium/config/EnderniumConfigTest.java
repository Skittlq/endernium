package com.skittlq.endernium.config;

import com.skittlq.endernium.client.vfx.EnderniumVfxRenderMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnderniumConfigTest {
    @Test
    void defaultsMatchGameplayDefaults() {
        EnderniumConfig config = new EnderniumConfig();
        assertTrue(config.enderniumArmorAbility);
        assertEquals(EnderniumGameplayConfig.DEFAULT_ARMOR_ABILITY_THRESHOLD,
                config.enderniumArmorAbilityThreshold);
        assertEquals(EnderniumGameplayConfig.DEFAULT_ARMOR_MAX_STORED_DAMAGE,
                config.enderniumArmorMaxStoredDamage);
        assertEquals(EnderniumGameplayConfig.DEFAULT_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER,
                config.enderniumSwordDamagePerStrikeMultiplier);
        assertEquals(EnderniumGameplayConfig.DEFAULT_SWORD_MAX_STRIKES,
                config.enderniumSwordMaxStrikes);
        assertEquals(EnderniumVfxRenderMode.AUTO, config.vfxRenderMode);
    }

    @Test
    void copyIsIndependent() {
        EnderniumConfig original = new EnderniumConfig();
        EnderniumConfig copy = original.copy();
        assertNotSame(original, copy);
        copy.enderniumArmorAbilityThreshold = 99;
        copy.vfxRenderMode = EnderniumVfxRenderMode.PARTICLES;
        assertEquals(EnderniumGameplayConfig.DEFAULT_ARMOR_ABILITY_THRESHOLD,
                original.enderniumArmorAbilityThreshold);
        assertEquals(EnderniumVfxRenderMode.AUTO, original.vfxRenderMode);
    }

    @Test
    void sanitizeRecoversNullAndBoundsValues() {
        EnderniumConfig defaults = EnderniumConfig.sanitize(null);
        assertEquals(EnderniumGameplayConfig.DEFAULT_ARMOR_ABILITY_THRESHOLD,
                defaults.enderniumArmorAbilityThreshold);

        EnderniumConfig invalid = new EnderniumConfig();
        invalid.enderniumArmorAbilityThreshold = Integer.MAX_VALUE;
        invalid.enderniumArmorMaxStoredDamage = Double.POSITIVE_INFINITY;
        invalid.enderniumSwordDamagePerStrikeMultiplier = -1.0D;
        invalid.enderniumSwordMaxStrikes = Integer.MAX_VALUE;
        invalid.enderniumSpearStrainDurationSeconds = Integer.MAX_VALUE;
        invalid.enderniumSpearMaximumHealthCostPercent = 200.0D;
        invalid.vfxRenderMode = null;
        EnderniumConfig sanitized = EnderniumConfig.sanitize(invalid);

        assertEquals(2048, sanitized.enderniumArmorAbilityThreshold);
        assertEquals(EnderniumGameplayConfig.DEFAULT_ARMOR_MAX_STORED_DAMAGE,
                sanitized.enderniumArmorMaxStoredDamage);
        assertEquals(EnderniumGameplayConfig.MIN_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER,
                sanitized.enderniumSwordDamagePerStrikeMultiplier);
        assertEquals(EnderniumGameplayConfig.MAX_SWORD_STRIKES, sanitized.enderniumSwordMaxStrikes);
        assertEquals(EnderniumGameplayConfig.MAX_SPEAR_STRAIN_DURATION_SECONDS,
                sanitized.enderniumSpearStrainDurationSeconds);
        assertEquals(100.0D, sanitized.enderniumSpearMaximumHealthCostPercent);
        assertEquals(EnderniumVfxRenderMode.AUTO, sanitized.vfxRenderMode);
    }
}
