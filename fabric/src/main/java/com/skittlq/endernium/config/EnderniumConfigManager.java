package com.skittlq.endernium.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.skittlq.endernium.Endernium;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EnderniumConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(Endernium.MOD_ID + ".json");

    private static EnderniumConfig config = new EnderniumConfig();

    private EnderniumConfigManager() {
    }

    public static void load() {
        if (Files.notExists(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            EnderniumConfig loaded = GSON.fromJson(reader, EnderniumConfig.class);
            config = sanitize(loaded);
        } catch (Exception exception) {
            Endernium.LOGGER.error("Failed to load config from {}. Using defaults.", CONFIG_PATH, exception);
            config = new EnderniumConfig();
            save();
        }
    }

    public static void save() {
        config = sanitize(config);
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException exception) {
            Endernium.LOGGER.error("Failed to save config to {}", CONFIG_PATH, exception);
        }
    }

    public static EnderniumConfig getConfig() {
        return config;
    }

    public static EnderniumConfig copyConfig() {
        return config.copy();
    }

    public static void setConfig(EnderniumConfig newConfig) {
        config = sanitize(newConfig);
    }

    private static EnderniumConfig sanitize(EnderniumConfig rawConfig) {
        EnderniumConfig sanitized = rawConfig == null ? new EnderniumConfig() : rawConfig.copy();
        sanitized.enderniumArmorAbilityThreshold = Math.max(1, sanitized.enderniumArmorAbilityThreshold);
        sanitized.enderniumArmorAbilityCooldown = Math.max(1L, sanitized.enderniumArmorAbilityCooldown);
        return sanitized;
    }
}
