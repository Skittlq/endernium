package com.skittlq.endernium.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnderniumCombatHooksTest {
    @Test
    void dragonDamageRequiresPositiveInflictedDamage() {
        assertFalse(EnderniumCombatHooks.shouldRecordDragonDamage(0.0F, true));
        assertFalse(EnderniumCombatHooks.shouldRecordDragonDamage(-1.0F, true));
        assertFalse(EnderniumCombatHooks.shouldRecordDragonDamage(1.0F, false));
        assertTrue(EnderniumCombatHooks.shouldRecordDragonDamage(0.5F, true));
    }

    @Test
    void pvpHitRequiresPositiveUnblockedDamage() {
        assertFalse(EnderniumCombatHooks.shouldRecordPvpHit(0.0F, false, true));
        assertFalse(EnderniumCombatHooks.shouldRecordPvpHit(1.0F, true, true));
        assertFalse(EnderniumCombatHooks.shouldRecordPvpHit(1.0F, false, false));
        assertTrue(EnderniumCombatHooks.shouldRecordPvpHit(1.0F, false, true));
    }
}
