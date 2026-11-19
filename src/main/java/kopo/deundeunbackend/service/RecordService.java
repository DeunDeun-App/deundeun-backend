package kopo.deundeunbackend.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kopo.deundeunbackend.domain.RecordKeyword;
import kopo.deundeunbackend.domain.RecordSession;
import kopo.deundeunbackend.domain.TrainingSession;
import kopo.deundeunbackend.dto.response.KeywordResponse;
import kopo.deundeunbackend.dto.response.RecordResponse;
import kopo.deundeunbackend.mapper.RecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 홈·기록 화면: 주별 횟수, 이번 주 훈련 시간, 최근 훈련과 키워드, 점수 변화, 항목별 달성률. */
@Service
@RequiredArgsConstructor
public class RecordService {

    // 최근 훈련 목록 최대 개수. 앱 "전체 보기" 와 같다
    private static final int RECORD_LIMIT = 15;

    // 점수 변화 그래프에 보여줄 최근 연습 수
    private static final int TREND_LIMIT = 6;

    private static final int WEEKS = 5;

    private static final String TITLE_CALL = "119 신고 연습";
    private static final String TITLE_SIMULATION = "재난 시뮬레이션";

    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("M/d");
    private static final DateTimeFormatter PERFORMED_AT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final RecordMapper recordMapper;

    private final SessionService sessionService;

    /**
     * 기록 화면 조회
     *
     * @param authorization 로그인 토큰 ("Bearer ...")
     */
    @Transactional(readOnly = true)
    public RecordResponse getRecords(String authorization) {
        Long userId = sessionService.requireUserId(authorization);

        LocalDate thisMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate firstMonday = thisMonday.minusWeeks(WEEKS - 1);

        List<String> weekLabels = new ArrayList<>();
        Integer[] simulationCounts = new Integer[WEEKS];
        Integer[] callCounts = new Integer[WEEKS];
        for (int i = 0; i < WEEKS; i++) {
            weekLabels.add(firstMonday.plusWeeks(i).format(SHORT_DATE));
            simulationCounts[i] = 0;
            callCounts[i] = 0;
        }
        long[] daySeconds = new long[7];
        for (RecordSession session : recordMapper.selectSessionsSince(userId, firstMonday.atStartOfDay())) {
            int week = (int) (ChronoUnit.DAYS.between(firstMonday, session.getStartedAt().toLocalDate()) / 7);
            if (week < 0 || week >= WEEKS) {
                continue;
            }
            if (isCall(session.getTrainingType())) {
                callCounts[week]++;
            } else {
                simulationCounts[week]++;
            }
            if (week == WEEKS - 1) {
                int day = session.getStartedAt().getDayOfWeek().getValue() - 1;
                daySeconds[day] += seconds(session);
            }
        }
        List<Integer> dayMinutes = new ArrayList<>();
        for (long seconds : daySeconds) {
            // 1분이 안 되는 연습도 막대에 보이도록 올림
            dayMinutes.add((int) ((seconds + 59) / 60));
        }

        List<RecordSession> recent = recordMapper.selectSessions(userId, null, RECORD_LIMIT);
        Map<Long, List<RecordKeyword>> keywordsBySession = recent.isEmpty()
                ? Map.of()
                : recordMapper.selectKeywords(userId, recent.stream().map(RecordSession::getSessionId).toList())
                        .stream()
                        .collect(Collectors.groupingBy(RecordKeyword::getSessionId));

        List<RecordResponse.Item> records = recent.stream()
                .map(session -> toItem(session, keywordsBySession.getOrDefault(session.getSessionId(), List.of())))
                .toList();

        List<RecordKeyword> allKeywords = recordMapper.selectKeywords(userId, null);

        return new RecordResponse(weekLabels, List.of(simulationCounts), List.of(callCounts), dayMinutes, records,
                report(userId, TrainingSession.TYPE_CALL119, allKeywords),
                report(userId, TrainingSession.TYPE_POSE, allKeywords));
    }

    private RecordResponse.Item toItem(RecordSession session, List<RecordKeyword> keywords) {
        List<KeywordResponse> chips = keywords.stream()
                .map(k -> new KeywordResponse(k.getLabel(), k.isPassed(), k.isPassed() ? null : firstLine(k.getFeedback())))
                .toList();

        return new RecordResponse.Item(session.getSessionId(), session.getTrainingType(),
                isCall(session.getTrainingType()) ? TITLE_CALL : TITLE_SIMULATION,
                session.getScenarioTitle(), session.getStartedAt().format(PERFORMED_AT),
                score(session), chips);
    }

    private RecordResponse.Report report(Long userId, String type, List<RecordKeyword> allKeywords) {
        List<RecordSession> latest = new ArrayList<>(recordMapper.selectSessions(userId, type, TREND_LIMIT));
        Collections.reverse(latest);

        List<String> labels = latest.stream().map(s -> s.getStartedAt().format(SHORT_DATE)).toList();
        List<Integer> scores = latest.stream().map(this::score).toList();

        // 처음 나온 순서(질문·단계 순서)대로 항목을 모은다
        Map<String, int[]> counts = new LinkedHashMap<>();
        allKeywords.stream()
                .filter(k -> type.equals(k.getTrainingType()))
                .sorted((a, b) -> Integer.compare(order(a), order(b)))
                .forEach(k -> {
                    int[] count = counts.computeIfAbsent(k.getStatLabel(), key -> new int[2]);
                    count[0]++;
                    if (k.isPassed()) {
                        count[1]++;
                    }
                });

        List<RecordResponse.Rate> rates = counts.entrySet().stream()
                .map(e -> new RecordResponse.Rate(e.getKey(), Math.round(e.getValue()[1] * 100f / e.getValue()[0])))
                .toList();

        return new RecordResponse.Report(labels, scores, rates);
    }

    private static int order(RecordKeyword keyword) {
        return keyword.getItemOrder() == null ? Integer.MAX_VALUE : keyword.getItemOrder();
    }

    private static long seconds(RecordSession session) {
        if (session.getEndedAt() == null) {
            return 0;
        }
        return Math.max(0, Duration.between(session.getStartedAt(), session.getEndedAt()).getSeconds());
    }

    private int score(RecordSession session) {
        return session.getScore() == null ? 0 : session.getScore().intValue();
    }

    private static boolean isCall(String trainingType) {
        return TrainingSession.TYPE_CALL119.equals(trainingType);
    }

    /** 신고 연습 피드백은 예시까지 여러 줄이라 첫 줄만 쓴다 */
    private static String firstLine(String text) {
        if (text == null) {
            return null;
        }
        int newline = text.indexOf('\n');
        return newline < 0 ? text : text.substring(0, newline);
    }
}
