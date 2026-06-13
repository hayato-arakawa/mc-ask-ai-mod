package com.example.askaimod.config;

import com.example.askaimod.AskAiMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config", AskAiMod.MOD_ID + ".json");

    private static AskAiModConfig config;

    public static AskAiModConfig getConfig() {
        if (config == null) {
            config = load();
        }
        return config;
    }

    public static AskAiModConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                return GSON.fromJson(json, AskAiModConfig.class);
            } catch (IOException e) {
                AskAiMod.LOGGER.error("Failed to load config", e);
            }
        }
        return new AskAiModConfig();
    }

    public static void save(AskAiModConfig newConfig) {
        config = newConfig;
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(newConfig);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            AskAiMod.LOGGER.error("Failed to save config", e);
        }
    }
}
