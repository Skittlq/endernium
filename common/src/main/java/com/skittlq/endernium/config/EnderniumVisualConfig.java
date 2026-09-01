package com.skittlq.endernium.config;

import java.util.Locale;
import java.util.Objects;

/** Client-only presentation settings shared by both loader frontends. */
public final class EnderniumVisualConfig {
    public enum EffectQuality {
        OFF,
        FAST,
        FANCY;

        public static EffectQuality parse(String value) {
            if (value == null) {
                return FANCY;
            }
            try {
                return valueOf(value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return FANCY;
            }
        }
    }

    private static Settings settings = new Settings() {
        @Override
        public EffectQuality quality() {
            return EffectQuality.FANCY;
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
        return quality() == EffectQuality.FANCY;
    }

    public interface Settings {
        EffectQuality quality();
    }
}
