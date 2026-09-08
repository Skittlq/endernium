package com.skittlq.endernium.client.vfx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Supplier;

/** Selects the shader or particle implementation without taking a hard dependency on Iris. */
public final class EnderniumVfxCompatibility {
    private static final Logger LOGGER = LoggerFactory.getLogger("Endernium/VfxCompatibility");
    private static final Supplier<EnderniumVfxRenderMode> DEFAULT_PREFERENCE = () -> EnderniumVfxRenderMode.AUTO;

    private static Supplier<EnderniumVfxRenderMode> preference = DEFAULT_PREFERENCE;
    private static ShaderPackDetector detector = new ReflectiveIrisDetector();
    private static boolean rendererFailed;
    private static boolean loggedDetectorFailure;

    private EnderniumVfxCompatibility() {
    }

    public static void bindPreference(Supplier<EnderniumVfxRenderMode> preferenceSupplier) {
        preference = Objects.requireNonNull(preferenceSupplier);
    }

    public static EnderniumVfxRenderMode effectiveMode() {
        if (rendererFailed) {
            return EnderniumVfxRenderMode.PARTICLES;
        }
        EnderniumVfxRenderMode requested;
        try {
            requested = preference.get();
        } catch (RuntimeException exception) {
            requested = EnderniumVfxRenderMode.AUTO;
        }
        if (requested == null) {
            requested = EnderniumVfxRenderMode.AUTO;
        }
        if (requested != EnderniumVfxRenderMode.AUTO) {
            return requested;
        }

        ShaderPackState state = detector.detect();
        if (state == ShaderPackState.UNKNOWN) {
            if (!loggedDetectorFailure) {
                loggedDetectorFailure = true;
                LOGGER.warn("Iris is present but its shader-pack state could not be queried; using safe Endernium particles");
            }
            return EnderniumVfxRenderMode.PARTICLES;
        }
        return state == ShaderPackState.ACTIVE
                ? EnderniumVfxRenderMode.PARTICLES
                : EnderniumVfxRenderMode.FULL;
    }

    public static boolean usesParticleFallback() {
        return effectiveMode() == EnderniumVfxRenderMode.PARTICLES;
    }

    public static void reportShaderFailure() {
        rendererFailed = true;
    }

    public static void resetRendererFailure() {
        rendererFailed = false;
    }

    public static void resetSession() {
        rendererFailed = false;
    }

    static void setDetectorForTests(ShaderPackDetector testDetector) {
        detector = Objects.requireNonNull(testDetector);
    }

    static void resetForTests() {
        preference = DEFAULT_PREFERENCE;
        detector = new ReflectiveIrisDetector();
        rendererFailed = false;
        loggedDetectorFailure = false;
    }

    enum ShaderPackState {
        INACTIVE,
        ACTIVE,
        UNKNOWN
    }

    @FunctionalInterface
    interface ShaderPackDetector {
        ShaderPackState detect();
    }

    private static final class ReflectiveIrisDetector implements ShaderPackDetector {
        private boolean initialized;
        private boolean irisAbsent;
        private Method getInstance;
        private Method isShaderPackInUse;

        @Override
        public ShaderPackState detect() {
            if (!initialized) {
                initialize();
            }
            if (irisAbsent) {
                return ShaderPackState.INACTIVE;
            }
            if (getInstance == null || isShaderPackInUse == null) {
                return ShaderPackState.UNKNOWN;
            }
            try {
                Object api = getInstance.invoke(null);
                return Boolean.TRUE.equals(isShaderPackInUse.invoke(api))
                        ? ShaderPackState.ACTIVE
                        : ShaderPackState.INACTIVE;
            } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
                return ShaderPackState.UNKNOWN;
            }
        }

        private void initialize() {
            initialized = true;
            try {
                Class<?> apiClass = Class.forName(
                        "net.irisshaders.iris.api.v0.IrisApi",
                        false,
                        EnderniumVfxCompatibility.class.getClassLoader()
                );
                getInstance = apiClass.getMethod("getInstance");
                isShaderPackInUse = apiClass.getMethod("isShaderPackInUse");
            } catch (ClassNotFoundException exception) {
                irisAbsent = true;
            } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
                getInstance = null;
                isShaderPackInUse = null;
            }
        }
    }
}
