package com.example.askaimod.config;

import com.google.gson.annotations.SerializedName;

public class AskAiModConfig {
    @SerializedName("endpoint")
    private String endpoint = "https://api.openai.com/v1";

    @SerializedName("api_key")
    private String apiKey = "";

    @SerializedName("model")
    private String model = "gpt-4o-mini";

    @SerializedName("temperature")
    private double temperature = 0.7;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}
