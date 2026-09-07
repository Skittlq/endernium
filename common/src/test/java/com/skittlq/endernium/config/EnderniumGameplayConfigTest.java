package com.skittlq.endernium.config;

import com.skittlq.endernium.item.armor.EnderniumArmorAbility;
import com.skittlq.endernium.util.EnderniumCooldowns;
import com.skittlq.endernium.client.EnderniumClientGameplaySettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnderniumGameplayConfigTest {
    @Test
    void cooldownConversionSaturatesWithoutOverflow() {
        assertEquals(Integer.MAX_VALUE, EnderniumArmorAbility.cooldownTicks(Long.MAX_VALUE));
        assertEquals(0L, EnderniumArmorAbility.cooldownTicks(0L));
        assertEquals(1_800L, EnderniumArmorAbility.cooldownTicks(90L));
    }

    @Test
    void swordCooldownCalculationSaturates() {
        EnderniumGameplayConfig.bind(new EnderniumGameplayConfig.Settings() {
            public boolean swordAbilityEnabled() { return true; }
            public int swordAbilityBaseCooldownSeconds() { return Integer.MAX_VALUE; }
            public int swordAbilityPerMobCooldownSeconds() { return Integer.MAX_VALUE; }
            public boolean toolsVeinMiningEnabled() { return true; }
            public boolean armorAbilityEnabled() { return true; }
            public int armorAbilityThreshold() { return 4; }
            public long armorAbilityCooldownSeconds() { return 90L; }
        });
        assertEquals(Integer.MAX_VALUE, EnderniumGameplayConfig.swordAbilityCooldownTicks(Integer.MAX_VALUE));
    }

    @Test
    void cooldownExpiryAndClockRollbackAreHandledConsistently() {
        assertTrue(EnderniumCooldowns.isDeadlineActive(109L, 110L, 10L));
        assertFalse(EnderniumCooldowns.isDeadlineActive(110L, 110L, 10L));
        assertFalse(EnderniumCooldowns.isDeadlineActive(50L, 110L, 10L));

        assertTrue(EnderniumCooldowns.isElapsedCooldownActive(109L, 100L, 10L));
        assertFalse(EnderniumCooldowns.isElapsedCooldownActive(110L, 100L, 10L));
        assertFalse(EnderniumCooldowns.isElapsedCooldownActive(50L, 100L, 10L));
        assertEquals(Long.MAX_VALUE, EnderniumCooldowns.deadline(Long.MAX_VALUE - 2L, 10L));
    }

    @Test
    void clientSafeDefaultsDoNotReadTheBoundLoaderConfig() {
        EnderniumGameplayConfig.bind(new EnderniumGameplayConfig.Settings() {
            private IllegalStateException unavailable() {
                return new IllegalStateException("config not loaded");
            }

            public boolean swordAbilityEnabled() { throw unavailable(); }
            public int swordAbilityBaseCooldownSeconds() { throw unavailable(); }
            public int swordAbilityPerMobCooldownSeconds() { throw unavailable(); }
            public boolean toolsVeinMiningEnabled() { throw unavailable(); }
            public boolean armorAbilityEnabled() { throw unavailable(); }
            public int armorAbilityThreshold() { throw unavailable(); }
            public long armorAbilityCooldownSeconds() { throw unavailable(); }
        });

        EnderniumClientGameplaySettings.reset();
        EnderniumGameplayConfig.Snapshot defaults = EnderniumClientGameplaySettings.get();
        assertTrue(defaults.swordAbilityEnabled());
        assertEquals(10, defaults.swordBaseCooldownSeconds());
        assertEquals(4, defaults.armorThreshold());
    }
}
