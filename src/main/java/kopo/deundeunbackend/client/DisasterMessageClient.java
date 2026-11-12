package kopo.deundeunbackend.client;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import kopo.deundeunbackend.domain.DisasterMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
public class DisasterMessageClient {

    private static final String ENDPOINT = "https://www.safetydata.go.kr/V2/api/DSSP-IF-00247";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private static final int FETCH_ROWS = 100;
    private static final int LOOKBACK_DAYS = 3;
    private static final DateTimeFormatter QUERY_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final JsonMapper jsonMapper;
    private final RestClient restClient = RestClients.create(Duration.ofSeconds(10));
    private final String serviceKey;
    private final String region;

    private List<DisasterMessage> cache = List.of();
    private Instant cachedAt = Instant.EPOCH;

    public DisasterMessageClient(JsonMapper jsonMapper,
                                 @Value("${deundeun.disaster.service-key:}") String serviceKey,
                                 @Value("${deundeun.disaster.region:}") String region) {
        this.jsonMapper = jsonMapper;
        this.serviceKey = serviceKey;
        this.region = region;
    }

    public synchronized List<DisasterMessage> fetchRecent(boolean refresh) {
        if (!refresh && !cache.isEmpty() && Instant.now().isBefore(cachedAt.plus(CACHE_TTL))) {
            return cache;
        }

        if (serviceKey == null || serviceKey.isBlank()) {
            log.warn("재난문자 API 서비스키가 없어 조회를 생략합니다. deundeun.disaster.service-key를 설정해 주세요.");
            return cache;
        }

        List<DisasterMessage> messages = requestMessages();
        if (!messages.isEmpty()) {
            cache = messages;
            cachedAt = Instant.now();
        }
        return cache;
    }

    private List<DisasterMessage> requestMessages() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(ENDPOINT)
                .queryParam("serviceKey", serviceKey)
                .queryParam("returnType", "json")
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", FETCH_ROWS)
                .queryParam("crtDt", LocalDate.now().minusDays(LOOKBACK_DAYS).format(QUERY_DATE));
        if (region != null && !region.isBlank()) {
            builder.queryParam("rgnNm", region);
        }
        URI uri = builder.encode().build().toUri();

        try {
            String response = restClient.get().uri(uri).retrieve().body(String.class);
            return parse(jsonMapper.readTree(response));

        } catch (RestClientException | JacksonException e) {
            log.warn("재난문자 API 호출 실패: {}", e.getMessage());
            return List.of();
        }
    }

    private List<DisasterMessage> parse(JsonNode root) {
        List<DisasterMessage> messages = new ArrayList<>();
        for (JsonNode row : root.path("body")) {
            String text = row.path("MSG_CN").asString("").trim();
            if (text.isEmpty()) {
                continue;
            }
            messages.add(DisasterMessage.of(
                    row.path("RCPTN_RGN_NM").asString("").trim(),
                    text,
                    row.path("EMRG_STEP_NM").asString(""),
                    row.path("DST_SE_NM").asString(""),
                    row.path("CRT_DT").asString("")));
        }

        if (messages.isEmpty()) {
            log.warn("재난문자 API 응답에 문자가 없습니다: {}", root.path("header"));
        }

        messages.sort(Comparator.comparing(DisasterMessage::createdAt).reversed());
        return messages;
    }
}
