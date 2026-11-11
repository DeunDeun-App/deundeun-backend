package kopo.deundeunbackend.client;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import kopo.deundeunbackend.common.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
public class GeminiClient {

    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";
    private static final int MAX_ATTEMPTS = 2;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);

    private final JsonMapper jsonMapper;
    private final RestClient restClient = RestClients.create(Duration.ofSeconds(30));
    private final String apiKey;
    private final String model;

    public GeminiClient(JsonMapper jsonMapper,
                        @Value("${deundeun.gemini.api-key:}") String apiKey,
                        @Value("${deundeun.gemini.model:gemini-3.5-flash-lite}") String model) {
        this.jsonMapper = jsonMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    public boolean isEnabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    public JsonNode generateJson(String systemPrompt, String userPrompt, Map<String, Object> schema) {
        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
                "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", userPrompt)))),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "responseMimeType", "application/json",
                        "responseSchema", schema));

        String response = request(jsonMapper.writeValueAsString(body));

        JsonNode root = jsonMapper.readTree(response);
        String text = root.path("candidates").path(0).path("content").path("parts").path(0)
                .path("text").asString("");

        if (text.isBlank()) {
            log.warn("Gemini 응답이 비어 있습니다: {}", response);
            throw unavailable();
        }

        return jsonMapper.readTree(text);
    }

    private String request(String body) {
        for (int attempt = 1; ; attempt++) {
            try {
                return restClient.post()
                        .uri(ENDPOINT, model)
                        .header("x-goog-api-key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(String.class);

            } catch (HttpServerErrorException | HttpClientErrorException.TooManyRequests e) {
                log.warn("Gemini 호출 실패({}회): {}", attempt, e.getStatusCode());
                if (attempt >= MAX_ATTEMPTS) {
                    throw unavailable();
                }
                sleep(RETRY_DELAY.multipliedBy(attempt));

            } catch (RestClientException e) {
                log.warn("Gemini 호출 실패: {}", e.getMessage());
                throw unavailable();
            }
        }
    }

    private void sleep(Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw unavailable();
        }
    }

    private ApiException unavailable() {
        return ApiException.unavailable("AI_UNAVAILABLE", "지금은 쉬운 말로 바꿀 수 없습니다. 잠시 후 다시 시도해 주세요.");
    }
}
