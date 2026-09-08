package com.skittlq.endernium.client.vfx;

import com.skittlq.endernium.client.vfx.EnderniumParticleFallback.EmissionCategory;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.BlessingState;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.BuildupState;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.BurstState;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager.ExtractedFrame;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnderniumParticleFallbackTest {
    @Test
    void budgetNeverExceedsOneHundredTwentyParticles() {
        EnderniumParticleFallback.EmissionBudget budget = new EnderniumParticleFallback.EmissionBudget(Integer.MAX_VALUE);
        int accepted = 0;
        while (budget.tryTake()) {
            accepted++;
        }
        assertEquals(EnderniumParticleFallback.MAX_PARTICLES_PER_TICK, accepted);
        assertEquals(0, budget.remaining());
    }

    @Test
    void reducedSettingsAndDistanceScaleNoncriticalEmission() {
        assertEquals(120, EnderniumParticleFallback.particleLimit("ALL"));
        assertEquals(72, EnderniumParticleFallback.particleLimit("DECREASED"));
        assertEquals(32, EnderniumParticleFallback.particleLimit("MINIMAL"));
        assertEquals(10, EnderniumParticleFallback.distanceScaledCount(10, 64.0));
        assertTrue(EnderniumParticleFallback.distanceScaledCount(10, 300.0) < 10);
        assertEquals(0, EnderniumParticleFallback.distanceScaledCount(10, 800.0));
    }

    @Test
    void samplingIsDeterministicAndChangesWithEpoch() {
        assertEquals(EnderniumParticleFallback.samplingSeed(42L, 7L), EnderniumParticleFallback.samplingSeed(42L, 7L));
        assertNotEquals(EnderniumParticleFallback.samplingSeed(42L, 7L), EnderniumParticleFallback.samplingSeed(42L, 8L));
    }

    @Test
    void activePhasesFollowPriorityOrder() {
        BlessingState blessing = new BlessingState(Vec3.ZERO, Vec3.ZERO, 2.0, Vec3.ZERO, Vec3.ZERO, 20.0F, 1L, 0, 1);
        BurstState burst = new BurstState(Vec3.ZERO, 2.0F, 2L, List.of(), false, null);
        ExtractedFrame frame = new ExtractedFrame(
                new BuildupState(Vec3.ZERO, 190.0F, 3L), burst, List.of(), List.of(), List.of(),
                0.5F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, List.of(blessing));
        assertEquals(List.of(EmissionCategory.BLESSING, EmissionCategory.WAVE_ARRIVAL,
                        EmissionCategory.DETONATION, EmissionCategory.BUILDUP, EmissionCategory.COMETS),
                EnderniumParticleFallback.activeCategories(frame));
    }
}
