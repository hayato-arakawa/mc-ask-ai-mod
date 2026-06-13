package com.example.askaimod.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatCompletionRequest {
    private final String model;

    private final List<ChatMessage> messages;

    @SerializedName("temperature")
    private final double temperature;

    @SerializedName("n")
    private final int n = 1;

    public ChatCompletionRequest(String model, List<ChatMessage> messages, double temperature) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
    }

    public String getModel() {
        return model;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getN() {
        return n;
    }
}
