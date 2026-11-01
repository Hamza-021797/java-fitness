package com.fitness.ai;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GeminiClient {
    private final RestClient rest;
    private final String apiKey;
    private final String model;

    public GeminiClient(@Value("${gemini.api-key:}") String apiKey, @Value("${gemini.model:gemini-2.5-flash}") String model) {
        var f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(Duration.ofSeconds(5));
        f.setReadTimeout(Duration.ofSeconds(40));
        this.rest = RestClient.builder().baseUrl("https://generativelanguage.googleapis.com").requestFactory(f).build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public boolean configured() { return apiKey != null && !apiKey.isBlank(); }

    public String generate(String prompt) {
        var body = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
        JsonNode res = rest.post().uri("/v1beta/models/{m}:generateContent", model)
            .header("x-goog-api-key", apiKey)
            .contentType(MediaType.APPLICATION_JSON).body(body)
            .retrieve().body(JsonNode.class);
        return res == null ? "" : res.at("/candidates/0/content/parts/0/text").asText("");
    }
}
