package com.example.askaimod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AskAiMod implements ModInitializer {
    public static final String MOD_ID = "ask-ai-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Ask AI Mod initialized");
    }
}
