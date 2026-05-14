package com.project.logtracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.logtracker.dto.analysis.AnalysisResult;
import com.project.logtracker.dto.analysis.SimilarIssue;
import com.project.logtracker.dto.issue.IssueResponse;
import com.project.logtracker.entity.IssueSeverity;
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
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final IssueService issueService;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.chat-model}")
    private String model;

    public AnalysisResult analyze(String rawLog, Long projectId) {
        List<SimilarIssue> similarIssues = findSimilarIssues(rawLog, projectId);
        return callOpenAi(rawLog, similarIssues);
    }

    private List<SimilarIssue> findSimilarIssues(String rawLog, Long projectId) {
        List<Float> vector = embeddingService.embed(rawLog);
        List<Long> candidateIds = vectorStoreService.search(vector, 3, projectId);

        return candidateIds.stream()
                .flatMap(id -> {
                    try {
                        IssueResponse issue = issueService.getById(id);
                        return java.util.stream.Stream.of(toSimilarIssue(issue));
                    } catch (Exception ignored) {
                        return java.util.stream.Stream.empty();
                    }
                })
                .toList();
    }

    private SimilarIssue toSimilarIssue(IssueResponse issue) {
        return new SimilarIssue(
                issue.id(),
                issue.title(),
                issue.analysis() != null ? issue.analysis().rootCause() : null,
                issue.resolution() != null ? issue.resolution().actualCause() : null,
                issue.resolution() != null ? issue.resolution().actionTaken() : null
        );
    }

    private AnalysisResult callOpenAi(String rawLog, List<SimilarIssue> similarIssues) {
        RestClient restClient = RestClient.builder()
                .baseUrl(OPENAI_ENDPOINT)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<String, Object> response = restClient.post()
                .body(buildRequestBody(rawLog, similarIssues))
                .retrieve()
                .body(Map.class);

        String content = extractContent(response);
        return parseStructuredResult(content, similarIssues);
    }

    private Map<String, Object> buildRequestBody(String rawLog, List<SimilarIssue> similarIssues) {
        String systemPrompt = """
                You are a backend log analysis assistant.

                Return strictly valid JSON only with these fields:
                summary, rootCause, recommendation, severity, confidence.

                Rules:
                - Write summary, rootCause, and recommendation in Korean.
                - severity must be one of LOW, MEDIUM, HIGH, CRITICAL.
                - Use CRITICAL for outage, data loss, payment failure, security incident, or repeated production impact.
                - Use HIGH for severe user-facing errors or persistent backend failures.
                - Use MEDIUM for degraded performance, intermittent errors, or recoverable failures.
                - Use LOW for minor warnings, low-risk validation issues, or informational anomalies.
                - Keep technical terms, class names, package names, SQL keywords,
                  exception names, server names, and log messages in English as-is.
                - Do not translate code syntax or stack trace content.
                - confidence must be a number between 0 and 1.
                - Do not return markdown.
                - Do not return explanations outside JSON.
                """;

        return Map.of(
                "model", model,
                "temperature", 0.2,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", buildUserPrompt(rawLog, similarIssues))
                )
        );
    }

    private String buildUserPrompt(String rawLog, List<SimilarIssue> similarIssues) {
        StringBuilder sb = new StringBuilder();

        if (!similarIssues.isEmpty()) {
            sb.append("--- 과거 유사 이슈 (참고용) ---\n\n");
            for (int i = 0; i < similarIssues.size(); i++) {
                SimilarIssue s = similarIssues.get(i);
                sb.append("[유사 이슈 ").append(i + 1).append("]\n");
                sb.append("제목: ").append(s.title()).append("\n");
                if (s.rootCause() != null && !s.rootCause().isBlank()) {
                    sb.append("추정 원인: ").append(s.rootCause()).append("\n");
                }
                if (s.actualCause() != null && !s.actualCause().isBlank()) {
                    sb.append("실제 원인: ").append(s.actualCause()).append("\n");
                }
                if (s.actionTaken() != null && !s.actionTaken().isBlank()) {
                    sb.append("조치 내용: ").append(s.actionTaken()).append("\n");
                }
                sb.append("\n");
            }
            sb.append("---\n\n");
            sb.append("위 유사 이슈를 참고하여 아래 로그를 분석하고 JSON으로 반환하세요:\n\n");
        } else {
            sb.append("Analyze this log and return JSON:\n\n");
        }

        sb.append(rawLog);
        return sb.toString();
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

    private AnalysisResult parseStructuredResult(String content, List<SimilarIssue> similarIssues) {
        try {
            JsonNode jsonNode = objectMapper.readTree(content);
            String summary = jsonNode.path("summary").asText("");
            String rootCause = jsonNode.path("rootCause").asText("");
            String recommendation = jsonNode.path("recommendation").asText("");
            IssueSeverity severity = parseSeverity(jsonNode.path("severity").asText("MEDIUM"));
            double confidence = jsonNode.path("confidence").asDouble(0.0);

            List<SimilarIssue> returnedSimilarIssues = similarIssues.isEmpty() ? null : similarIssues;
            return new AnalysisResult(summary, rootCause, recommendation, severity, confidence, returnedSimilarIssues);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse analysis result", e);
        }
    }

    private IssueSeverity parseSeverity(String severity) {
        try {
            return IssueSeverity.valueOf(severity.toUpperCase());
        } catch (Exception e) {
            return IssueSeverity.MEDIUM;
        }
    }
}
