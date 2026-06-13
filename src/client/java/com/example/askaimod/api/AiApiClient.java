package com.example.askaimod.api;

import com.example.askaimod.AskAiMod;
import com.example.askaimod.config.AskAiModConfig;
import com.example.askaimod.config.ConfigManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AiApiClient {
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public static CompletableFuture<String> sendMessage(List<ChatMessage> messages) {
        AskAiModConfig config = ConfigManager.getConfig();

        return CompletableFuture.supplyAsync(() -> {
            try {
                String endpoint = config.getEndpoint().replaceAll("/+$", "") + "/chat/completions";

                ChatCompletionRequest request = new ChatCompletionRequest(
                        config.getModel(), messages, config.getTemperature());

                String jsonBody = GSON.toJson(request);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + config.getApiKey())
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .timeout(Duration.ofSeconds(60))
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(httpRequest,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    AskAiMod.LOGGER.error("API error: {} {}", response.statusCode(), response.body());
                    return "Error: API returned status " + response.statusCode();
                }

                ChatCompletionResponse completion = GSON.fromJson(
                        response.body(), ChatCompletionResponse.class);

                String content = completion.getFirstChoiceContent();
                return content != null ? content : "Error: No response from AI";
            } catch (Exception e) {
                AskAiMod.LOGGER.error("API call failed", e);
                return "Error: " + e.getMessage();
            }
        });
    }
}
