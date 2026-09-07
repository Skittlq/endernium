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
import java.nio.file.StandardCopyOption;
import java.time.Instant;

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
            preserveInvalidConfig();
            config = new EnderniumConfig();
            save();
        }
    }

    public static void save() {
        config = sanitize(config);
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Path temporaryPath = Files.createTempFile(CONFIG_PATH.getParent(), Endernium.MOD_ID + "-", ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporaryPath)) {
                GSON.toJson(config, writer);
            }
            try {
                Files.move(temporaryPath, CONFIG_PATH,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                Files.move(temporaryPath, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
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

    private static void preserveInvalidConfig() {
        if (Files.notExists(CONFIG_PATH)) {
            return;
        }
        Path backup = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".invalid-"
                + Instant.now().toEpochMilli());
        try {
            Files.move(CONFIG_PATH, backup, StandardCopyOption.REPLACE_EXISTING);
            Endernium.LOGGER.warn("Preserved invalid Endernium config as {}", backup);
        } catch (IOException backupException) {
            Endernium.LOGGER.error("Failed to preserve invalid config {}", CONFIG_PATH, backupException);
        }
    }

    private static EnderniumConfig sanitize(EnderniumConfig rawConfig) {
        EnderniumConfig sanitized = rawConfig == null ? new EnderniumConfig() : rawConfig.copy();
        sanitized.enderniumArmorAbilityThreshold = Math.max(1,
                Math.min(2048, sanitized.enderniumArmorAbilityThreshold));
        sanitized.enderniumArmorAbilityCooldown = Math.max(1L,
                Math.min(EnderniumGameplayConfig.MAX_COOLDOWN_SECONDS,
                        sanitized.enderniumArmorAbilityCooldown));
        sanitized.enderniumSwordAbilityBaseCooldown = Math.max(0,
                Math.min(EnderniumGameplayConfig.MAX_COOLDOWN_SECONDS,
                        sanitized.enderniumSwordAbilityBaseCooldown));
        sanitized.enderniumSwordAbilityPerMobCooldown = Math.max(0,
                Math.min(EnderniumGameplayConfig.MAX_COOLDOWN_SECONDS,
                        sanitized.enderniumSwordAbilityPerMobCooldown));
        return sanitized;
    }
}
