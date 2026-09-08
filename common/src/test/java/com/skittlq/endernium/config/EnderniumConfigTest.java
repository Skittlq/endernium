package com.skittlq.endernium.config;

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
        assertEquals(EnderniumGameplayConfig.DEFAULT_ARMOR_ABILITY_COOLDOWN_SECONDS,
                config.enderniumArmorAbilityCooldown);
        assertEquals(EnderniumGameplayConfig.DEFAULT_SWORD_ABILITY_BASE_COOLDOWN_SECONDS,
                config.enderniumSwordAbilityBaseCooldown);
    }

    @Test
    void copyIsIndependent() {
        EnderniumConfig original = new EnderniumConfig();
        EnderniumConfig copy = original.copy();
        assertNotSame(original, copy);
        copy.enderniumArmorAbilityThreshold = 99;
        assertEquals(EnderniumGameplayConfig.DEFAULT_ARMOR_ABILITY_THRESHOLD,
                original.enderniumArmorAbilityThreshold);
    }

    @Test
    void sanitizeRecoversNullAndBoundsValues() {
        EnderniumConfig defaults = EnderniumConfig.sanitize(null);
        assertEquals(EnderniumGameplayConfig.DEFAULT_ARMOR_ABILITY_THRESHOLD,
                defaults.enderniumArmorAbilityThreshold);

        EnderniumConfig invalid = new EnderniumConfig();
        invalid.enderniumArmorAbilityThreshold = Integer.MAX_VALUE;
        invalid.enderniumArmorAbilityCooldown = Long.MAX_VALUE;
        invalid.enderniumSwordAbilityBaseCooldown = -1;
        invalid.enderniumSwordAbilityPerMobCooldown = Integer.MAX_VALUE;
        EnderniumConfig sanitized = EnderniumConfig.sanitize(invalid);

        assertEquals(2048, sanitized.enderniumArmorAbilityThreshold);
        assertEquals(EnderniumGameplayConfig.MAX_COOLDOWN_SECONDS, sanitized.enderniumArmorAbilityCooldown);
        assertEquals(0, sanitized.enderniumSwordAbilityBaseCooldown);
        assertEquals(EnderniumGameplayConfig.MAX_COOLDOWN_SECONDS,
                sanitized.enderniumSwordAbilityPerMobCooldown);
    }
}
