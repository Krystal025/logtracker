package com.project.logtracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private static final String OPENAI_EMBEDDING_ENDPOINT = "https://api.openai.com/v1/embeddings";

    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.embedding-model}")
    private String embeddingModel;

    public List<Float> embed(String text) {
        RestClient restClient = RestClient.builder()
                .baseUrl(OPENAI_EMBEDDING_ENDPOINT)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<?, ?> response = restClient.post()
                .body(Map.of("model", embeddingModel, "input", text))
                .retrieve()
                .body(Map.class);

        return parseEmbedding(response);
    }

    private List<Float> parseEmbedding(Map<?, ?> responseBody) {
        try {
            String json = objectMapper.writeValueAsString(responseBody);
            JsonNode root = objectMapper.readTree(json);
            JsonNode embeddingNode = root.path("data").get(0).path("embedding");

            List<Float> vector = new ArrayList<>();
            for (JsonNode val : embeddingNode) {
                vector.add((float) val.asDouble());
            }
            return vector;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse embedding response", e);
        }
    }
}
