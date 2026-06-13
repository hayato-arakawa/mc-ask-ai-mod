package com.example.askaimod.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            AskAiModConfig config = ConfigManager.getConfig();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.literal("Ask AI Mod Config"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

            general.addEntry(entryBuilder.startStrField(
                    Component.literal("API Endpoint"),
                    config.getEndpoint()
            ).setDefaultValue("https://api.openai.com/v1")
                    .setSaveConsumer(config::setEndpoint)
                    .build());

            general.addEntry(entryBuilder.startStrField(
                    Component.literal("API Key"),
                    config.getApiKey()
            ).setDefaultValue("")
                    .setSaveConsumer(config::setApiKey)
                    .build());

            general.addEntry(entryBuilder.startStrField(
                    Component.literal("Model"),
                    config.getModel()
            ).setDefaultValue("gpt-4o-mini")
                    .setSaveConsumer(config::setModel)
                    .build());

            general.addEntry(entryBuilder.startDoubleField(
                    Component.literal("Temperature"),
                    config.getTemperature()
            ).setDefaultValue(0.7)
                    .setMin(0.0)
                    .setMax(2.0)
                    .setSaveConsumer(config::setTemperature)
                    .build());

            builder.setSavingRunnable(() -> ConfigManager.save(config));

            return builder.build();
        };
    }
}
