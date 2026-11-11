package kopo.deundeunbackend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import kopo.deundeunbackend.client.DisasterMessageClient;
import kopo.deundeunbackend.client.GeminiClient;
import kopo.deundeunbackend.common.exception.ApiException;
import kopo.deundeunbackend.domain.DisasterMessage;
import kopo.deundeunbackend.domain.DisasterType;
import kopo.deundeunbackend.domain.MessageTranslation;
import kopo.deundeunbackend.domain.Pictogram;
import kopo.deundeunbackend.dto.response.DisasterMessageResponse;
import kopo.deundeunbackend.dto.response.EasyMessageResponse;
import kopo.deundeunbackend.dto.response.EasyMessageResponse.Item;
import kopo.deundeunbackend.mapper.DisasterMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisasterMessageService {

    private static final int RECENT_LIMIT = 10;
    private static final int MAX_ITEMS = 3;
    private static final int MAX_ACTIONS = 3;
    private static final String ETC_CODE = "ETC";
    private static final DateTimeFormatter SENT_AT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private static final Map<String, String> KEYWORDS = keywords();

    private static final String SYSTEM_PROMPT = """
            너는 발달장애인을 위한 재난 안전 앱 '든든'의 재난문자 도우미야.
            어려운 재난문자를 발달장애인이 바로 이해하고 행동할 수 있게 쉬운 말로 바꿔.

            규칙
            - 모든 문장은 '~해요', '~하세요' 같은 부드러운 해요체로 짧게 써. title도 명사로 끝내지 말고 '~해요', '~하세요'로 끝나는 문장으로 써.
            - 한자어, 전문용어, 영어, 숫자 단위는 쉬운 우리말로 바꿔.
              (예: 호우주의보 -> 비가 많이 와요, 하천 -> 강, 침수 -> 물에 잠겨요, 대피 -> 안전한 곳으로 피해요, 즉시 -> 지금 바로, 발효/발령/권고 -> 쓰지 않기)
            - '절대', '반드시' 같은 겁주는 말 대신 차분하고 다정하게 써.
            - 문자에 없는 내용을 지어내지 마. 지역 이름은 문자에 있는 그대로 써.
            - items는 2~3개. 첫 번째는 무슨 일이 생겼는지, 나머지는 가장 중요한 주의사항이야.
            - items의 title은 15자 이내, description은 한두 문장, 40자 이내로 써.
            - pictogram은 반드시 주어진 그림 코드 중 내용에 가장 잘 맞는 것을 골라.
            - actions는 지금 바로 해야 할 행동 1~3개. 각각 한 문장, 25자 이내로 써.
            - disasterCode는 주어진 재난 코드 중 하나를 골라. 맞는 것이 없으면 ETC.
            - ttsText는 소리 내어 읽어줄 문장이야. 무슨 일인지와 해야 할 행동을 3문장 이내로 이어서 써.
            """;

    private final DisasterMessageClient disasterMessageClient;

    private final GeminiClient geminiClient;

    private final DisasterMapper disasterMapper;

    private final JsonMapper jsonMapper;

    public List<DisasterMessageResponse> getRecentMessages(boolean refresh) {
        return disasterMessageClient.fetchRecent(refresh).stream()
                .limit(RECENT_LIMIT)
                .map(DisasterMessageResponse::from)
                .toList();
    }

    public EasyMessageResponse toEasyMessage(String id) {
        DisasterMessage message = findMessage(id);
        Map<String, DisasterType> types = disasterTypes();

        MessageTranslation saved = disasterMapper.selectTranslationByHash(id);
        if (saved != null) {
            return fromSaved(saved, types);
        }

        if (!geminiClient.isEnabled()) {
            return basicMessage(message, types);
        }

        MessageTranslation translation;
        try {
            translation = translate(message, types);
        } catch (ApiException e) {
            log.warn("AI 변환 실패로 기본 행동요령을 안내합니다: hash={}, code={}", id, e.getCode());
            return basicMessage(message, types);
        }
        disasterMapper.insertTranslation(translation);
        log.info("재난문자 쉬운 말 변환 저장: hash={}, code={}", id, translation.getDisasterCode());

        return fromSaved(translation, types);
    }

    private DisasterMessage findMessage(String id) {
        return findIn(disasterMessageClient.fetchRecent(false), id)
                .or(() -> findIn(disasterMessageClient.fetchRecent(true), id))
                .orElseThrow(() -> ApiException.notFound("MESSAGE_NOT_FOUND",
                        "재난문자를 찾을 수 없습니다. 목록을 새로 불러와 주세요."));
    }

    private Optional<DisasterMessage> findIn(List<DisasterMessage> messages, String id) {
        return messages.stream().filter(message -> message.id().equals(id)).findFirst();
    }

    private MessageTranslation translate(DisasterMessage message, Map<String, DisasterType> types) {
        List<Pictogram> pictograms = disasterMapper.selectActivePictograms();
        Set<String> pictogramCodes = pictograms.stream().map(Pictogram::getPictogramCode).collect(Collectors.toSet());

        JsonNode result = geminiClient.generateJson(SYSTEM_PROMPT, userPrompt(message, types, pictograms),
                schema(types.keySet(), pictogramCodes));

        String disasterCode = result.path("disasterCode").asString(ETC_CODE);
        if (!types.containsKey(disasterCode)) {
            disasterCode = ETC_CODE;
        }

        List<Item> items = new ArrayList<>();
        for (JsonNode node : result.path("items")) {
            String title = node.path("title").asString("").trim();
            if (title.isEmpty() || items.size() >= MAX_ITEMS) {
                continue;
            }
            String pictogram = node.path("pictogram").asString("");
            items.add(new Item(pictogramCodes.contains(pictogram) ? pictogram : "INFO", title,
                    node.path("description").asString("").trim()));
        }

        List<String> actions = new ArrayList<>();
        for (JsonNode node : result.path("actions")) {
            String action = node.asString("").trim();
            if (!action.isEmpty() && actions.size() < MAX_ACTIONS) {
                actions.add(action);
            }
        }

        if (items.isEmpty()) {
            log.warn("Gemini 결과에 쉬운 말 항목이 없습니다: {}", result);
            throw ApiException.unavailable("AI_UNAVAILABLE", "지금은 쉬운 말로 바꿀 수 없습니다. 잠시 후 다시 시도해 주세요.");
        }

        return MessageTranslation.builder()
                .msgHash(message.id())
                .disasterCode(disasterCode)
                .region(truncate(message.region(), 50))
                .originalText(message.text())
                .easyText(jsonMapper.writeValueAsString(items))
                .ttsText(result.path("ttsText").asString(""))
                .actionText(String.join("\n", actions))
                .pictogramCodes(jsonMapper.writeValueAsString(items.stream().map(Item::pictogram).toList()))
                .receivedAt(parseSentAt(message.createdAt()))
                .build();
    }

    private String userPrompt(DisasterMessage message, Map<String, DisasterType> types, List<Pictogram> pictograms) {
        String typeLines = types.values().stream()
                .map(t -> "- " + t.getDisasterCode() + " (" + t.getDisasterName() + "): " + t.getGuideText())
                .collect(Collectors.joining("\n"));
        String pictogramLines = pictograms.stream()
                .map(p -> "- " + p.getPictogramCode() + ": " + p.getPictogramName())
                .collect(Collectors.joining("\n"));

        return "재난문자\n" + message.text()
                + "\n\n받은 지역: " + message.region()
                + "\n재난 구분: " + message.category()
                + "\n\n재난 코드와 국민행동요령\n" + typeLines
                + "\n\n사용할 수 있는 그림 코드\n" + pictogramLines;
    }

    private Map<String, Object> schema(Set<String> disasterCodes, Set<String> pictogramCodes) {
        Map<String, Object> item = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "pictogram", Map.of("type", "STRING", "enum", List.copyOf(pictogramCodes)),
                        "title", Map.of("type", "STRING"),
                        "description", Map.of("type", "STRING")),
                "required", List.of("pictogram", "title", "description"));

        return Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "disasterCode", Map.of("type", "STRING", "enum", List.copyOf(disasterCodes)),
                        "items", Map.of("type", "ARRAY", "items", item),
                        "actions", Map.of("type", "ARRAY", "items", Map.of("type", "STRING")),
                        "ttsText", Map.of("type", "STRING")),
                "required", List.of("disasterCode", "items", "actions", "ttsText"));
    }

    private EasyMessageResponse fromSaved(MessageTranslation saved, Map<String, DisasterType> types) {
        List<Item> items = jsonMapper.readValue(saved.getEasyText(), new TypeReference<List<Item>>() {
        });
        List<String> actions = saved.getActionText() == null || saved.getActionText().isBlank()
                ? List.of()
                : Arrays.asList(saved.getActionText().split("\n"));

        return new EasyMessageResponse(saved.getMsgHash(), saved.getDisasterCode(),
                disasterName(types, saved.getDisasterCode()), items, actions, saved.getTtsText(), true);
    }

    private EasyMessageResponse basicMessage(DisasterMessage message, Map<String, DisasterType> types) {
        DisasterType type = types.getOrDefault(classify(message), types.get(ETC_CODE));
        String code = type == null ? ETC_CODE : type.getDisasterCode();
        String easyName = type == null ? "안전 안내예요" : type.getEasyName();
        String guide = type == null || type.getGuideText() == null ? "" : type.getGuideText();

        List<String> actions = Arrays.stream(guide.split("(?<=\\.)\\s*"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .limit(MAX_ACTIONS)
                .toList();

        String description = message.region() == null || message.region().isBlank()
                ? "재난문자가 왔어요"
                : message.region() + "에 재난문자가 왔어요";
        List<Item> items = List.of(new Item(type == null ? "INFO" : type.getIcon(), easyName, description));

        return new EasyMessageResponse(message.id(), code, disasterName(types, code), items, actions,
                easyName + ". " + String.join(" ", actions), false);
    }

    private String classify(DisasterMessage message) {
        String source = (message.category() == null ? "" : message.category()) + " " + message.text();
        return KEYWORDS.entrySet().stream()
                .filter(entry -> source.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(ETC_CODE);
    }

    private Map<String, DisasterType> disasterTypes() {
        Map<String, DisasterType> types = new LinkedHashMap<>();
        for (DisasterType type : disasterMapper.selectDisasterTypes()) {
            types.put(type.getDisasterCode(), type);
        }
        return types;
    }

    private String disasterName(Map<String, DisasterType> types, String code) {
        DisasterType type = types.get(code);
        return type == null ? "기타" : type.getDisasterName();
    }

    private LocalDateTime parseSentAt(String value) {
        try {
            return value == null || value.isBlank() ? null : LocalDateTime.parse(value.trim(), SENT_AT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }

    private static Map<String, String> keywords() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("산불", "WILDFIRE");
        map.put("화재", "FIRE");
        map.put("지진", "EARTHQUAKE");
        map.put("태풍", "TYPHOON");
        map.put("호우", "HEAVY_RAIN");
        map.put("홍수", "FLOOD");
        map.put("범람", "FLOOD");
        map.put("산사태", "LANDSLIDE");
        map.put("폭염", "HEAT");
        map.put("한파", "COLD");
        map.put("대설", "SNOW");
        map.put("강풍", "STRONG_WIND");
        map.put("미세먼지", "FINE_DUST");
        map.put("감염", "INFECTIOUS");
        map.put("실종", "MISSING");
        map.put("배회", "MISSING");
        map.put("통제", "TRAFFIC");
        return map;
    }
}
