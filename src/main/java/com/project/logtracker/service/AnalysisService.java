package com.project.logtracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.logtracker.dto.analysis.AnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private static final String OPENAI_ENDPOINT = "https://api.openai.com/v1/chat/completions";

    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    public AnalysisResult analyze(String rawLog) {
        RestClient restClient = RestClient.builder()
                .baseUrl(OPENAI_ENDPOINT)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<String, Object> response = restClient.post()
                .body(buildRequestBody(rawLog))
                .retrieve()
                .body(Map.class);

        String content = extractContent(response);
        return parseStructuredResult(content);
    }

    private Map<String, Object> buildRequestBody(String rawLog) {
        String systemPrompt = """
            You are a backend log analysis assistant.

            Return strictly valid JSON only with these fields:
            summary, rootCause, recommendation, confidence.

            Rules:
            - Write summary, rootCause, and recommendation in Korean.
            - Keep technical terms, class names, package names, SQL keywords,
              exception names, server names, and log messages in English as-is.
            - Do not translate code syntax or stack trace content.
            - confidence must be a number between 0 and 1.
            - Do not return markdown.
            - Do not return explanations outside JSON.
            """;

        String userPrompt = """
            Analyze this log and return JSON:

            %s
            """.formatted(rawLog);

        return Map.of(
                "model", model,
                "temperature", 0.2,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );
    }

    private String extractContent(Map<String, Object> responseBody) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("OpenAI response has no choices");
        }

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null || message.get("content") == null) {
            throw new IllegalStateException("OpenAI response content is empty");
        }
        return message.get("content").toString();
    }

    private AnalysisResult parseStructuredResult(String content) {
        try {
            JsonNode jsonNode = objectMapper.readTree(content);
            String summary = jsonNode.path("summary").asText("");
            String rootCause = jsonNode.path("rootCause").asText("");
            String recommendation = jsonNode.path("recommendation").asText("");
            double confidence = jsonNode.path("confidence").asDouble(0.0);

            return new AnalysisResult(summary, rootCause, recommendation, confidence);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse analysis result", e);
        }
    }
}
