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
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
    private static final int FETCH_ROWS = 30;
    private static final int LOOKBACK_DAYS = 7;
    private static final DateTimeFormatter QUERY_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final List<DisasterMessage> SAMPLES = List.of(
            new DisasterMessage("경기도 수원시", "[행정안전부] 경기 수원시 지역에 호우주의보 발효. 침수 위험지역 주민 대피 권고. 하천 인근 접근 금지. 안전한 곳으로 즉시 이동 바랍니다.",
                    "안전안내", "호우", "2026/07/17 10:05:00"),
            new DisasterMessage("서울특별시", "[서울특별시] 오늘 14시 폭염경보 발령. 야외활동 자제, 충분한 물 마시기 등 건강관리에 유의 바랍니다.",
                    "안전안내", "폭염", "2026/07/16 11:20:00"),
            new DisasterMessage("경상북도 경주시", "[기상청] 10:32 경북 경주시 남남서쪽 9km 지역 규모3.8 지진 발생. 낙하물로부터 몸을 보호하시기 바랍니다.",
                    "긴급재난", "지진", "2026/07/15 10:33:00"));

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final RestClient restClient;
    private final String serviceKey;
    private final String region;

    private List<DisasterMessage> cache = List.of();
    private Instant cachedAt = Instant.EPOCH;

    public DisasterMessageClient(@Value("${deundeun.disaster.service-key:}") String serviceKey,
                                 @Value("${deundeun.disaster.region:}") String region) {
        this.serviceKey = serviceKey;
        this.region = region;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    public synchronized List<DisasterMessage> fetchRecent(boolean refresh) {
        if (!refresh && !cache.isEmpty() && Instant.now().isBefore(cachedAt.plus(CACHE_TTL))) {
            return cache;
        }

        List<DisasterMessage> messages = serviceKey == null || serviceKey.isBlank() ? SAMPLES : requestMessages();
        if (!messages.isEmpty()) {
            cache = messages;
            cachedAt = Instant.now();
        }
        return messages.isEmpty() ? cache : messages;
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
        JsonNode rows = root.path("body");
        if (rows.isObject()) {
            rows = rows.path("items").isMissingNode() ? rows.path("item") : rows.path("items");
        }

        List<DisasterMessage> messages = new ArrayList<>();
        for (JsonNode row : rows) {
            String text = row.path("MSG_CN").asString("").trim();
            if (text.isEmpty()) {
                continue;
            }
            messages.add(new DisasterMessage(
                    row.path("RCPTN_RGN_NM").asString(""),
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
