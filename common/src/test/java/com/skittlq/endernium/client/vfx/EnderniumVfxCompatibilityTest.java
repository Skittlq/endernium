package com.skittlq.endernium.client.vfx;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnderniumVfxCompatibilityTest {
    @AfterEach
    void resetState() {
        EnderniumVfxCompatibility.resetForTests();
    }

    @Test
    void autoUsesFullWhenIrisIsAbsentOrInactive() {
        EnderniumVfxCompatibility.setDetectorForTests(() -> EnderniumVfxCompatibility.ShaderPackState.INACTIVE);
        assertEquals(EnderniumVfxRenderMode.FULL, EnderniumVfxCompatibility.effectiveMode());
    }

    @Test
    void autoUsesParticlesForAnActivePack() {
        EnderniumVfxCompatibility.setDetectorForTests(() -> EnderniumVfxCompatibility.ShaderPackState.ACTIVE);
        assertEquals(EnderniumVfxRenderMode.PARTICLES, EnderniumVfxCompatibility.effectiveMode());
    }

    @Test
    void manualOverridesIgnoreShaderPackDetection() {
        EnderniumVfxCompatibility.setDetectorForTests(() -> EnderniumVfxCompatibility.ShaderPackState.ACTIVE);
        EnderniumVfxCompatibility.bindPreference(() -> EnderniumVfxRenderMode.FULL);
        assertEquals(EnderniumVfxRenderMode.FULL, EnderniumVfxCompatibility.effectiveMode());
        EnderniumVfxCompatibility.setDetectorForTests(() -> EnderniumVfxCompatibility.ShaderPackState.INACTIVE);
        EnderniumVfxCompatibility.bindPreference(() -> EnderniumVfxRenderMode.PARTICLES);
        assertEquals(EnderniumVfxRenderMode.PARTICLES, EnderniumVfxCompatibility.effectiveMode());
    }

    @Test
    void detectorFailureFailsSafe() {
        EnderniumVfxCompatibility.setDetectorForTests(() -> EnderniumVfxCompatibility.ShaderPackState.UNKNOWN);
        assertEquals(EnderniumVfxRenderMode.PARTICLES, EnderniumVfxCompatibility.effectiveMode());
    }

    @Test
    void rendererFailureOverridesForcedFullUntilReset() {
        EnderniumVfxCompatibility.bindPreference(() -> EnderniumVfxRenderMode.FULL);
        EnderniumVfxCompatibility.reportShaderFailure();
        assertEquals(EnderniumVfxRenderMode.PARTICLES, EnderniumVfxCompatibility.effectiveMode());
        EnderniumVfxCompatibility.resetRendererFailure();
        assertEquals(EnderniumVfxRenderMode.FULL, EnderniumVfxCompatibility.effectiveMode());
    }
}
