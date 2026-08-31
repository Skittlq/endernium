package com.skittlq.endernium.config;

import java.util.Locale;
import java.util.Objects;

/** Client-only presentation settings shared by both loader frontends. */
public final class EnderniumVisualConfig {
    public enum EffectQuality {
        OFF,
        PERFORMANCE,
        CINEMATIC;

        public static EffectQuality parse(String value) {
            if (value == null) {
                return CINEMATIC;
            }
            try {
                return valueOf(value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return CINEMATIC;
            }
        }
    }

    private static Settings settings = new Settings() {
        @Override
        public EffectQuality quality() {
            return EffectQuality.CINEMATIC;
        }

        @Override
        public boolean screenDistortion() {
            return true;
        }

        @Override
        public boolean observerScreenEffects() {
            return true;
        }
    };

    private EnderniumVisualConfig() {
    }

    public static void bind(Settings newSettings) {
        settings = Objects.requireNonNull(newSettings);
    }

    public static EffectQuality quality() {
        return settings.quality();
    }

    public static boolean enabled() {
        return quality() != EffectQuality.OFF;
    }

    public static boolean cinematic() {
        return quality() == EffectQuality.CINEMATIC;
    }

    public static boolean screenDistortion() {
        return enabled() && cinematic() && settings.screenDistortion();
    }

    public static boolean observerScreenEffects() {
        return screenDistortion() && settings.observerScreenEffects();
    }

    public interface Settings {
        EffectQuality quality();

        boolean screenDistortion();

        boolean observerScreenEffects();
    }
}
