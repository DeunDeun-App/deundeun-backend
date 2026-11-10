package kopo.deundeunbackend.client;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import kopo.deundeunbackend.common.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
public class GeminiClient {

    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public GeminiClient(@Value("${deundeun.gemini.api-key:}") String apiKey,
                        @Value("${deundeun.gemini.model:gemini-3.5-flash-lite}") String model) {
        this.apiKey = apiKey;
        this.model = model;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(30));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
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

        try {
            String response = restClient.post()
                    .uri(ENDPOINT, model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonMapper.writeValueAsString(body))
                    .retrieve()
                    .body(String.class);

            JsonNode root = jsonMapper.readTree(response);
            String text = root.path("candidates").path(0).path("content").path("parts").path(0)
                    .path("text").asString("");

            if (text.isBlank()) {
                log.warn("Gemini 응답이 비어 있습니다: {}", response);
                throw unavailable();
            }

            return jsonMapper.readTree(text);

        } catch (RestClientException e) {
            log.warn("Gemini 호출 실패: {}", e.getMessage());
            throw unavailable();
        }
    }

    private ApiException unavailable() {
        return ApiException.unavailable("AI_UNAVAILABLE", "지금은 쉬운 말로 바꿀 수 없습니다. 잠시 후 다시 시도해 주세요.");
    }
}
