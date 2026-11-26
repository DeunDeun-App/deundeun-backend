package kopo.deundeunbackend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kopo.deundeunbackend.common.exception.ApiException;
import kopo.deundeunbackend.domain.PoseStep;
import kopo.deundeunbackend.domain.SessionResult;
import kopo.deundeunbackend.domain.TrainingSession;
import kopo.deundeunbackend.dto.request.SimulationRequest;
import kopo.deundeunbackend.dto.response.KeywordResponse;
import kopo.deundeunbackend.dto.response.SimulationResultResponse;
import kopo.deundeunbackend.mapper.SimulationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * 재난 시뮬레이션 결과 채점과 저장
 *
 * 100점 만점 = 동작 80점 + 시간 20점 − 잘못 고른 감점
 * - 동작 80점은 동작 키워드끼리 똑같이 나눈다 (지진: 웅크리기 40 + 머리 보호 40, 화재: 코·입 막기 80)
 * - 시간: {@link #FAST_MS} 안에 성공하면 +20, {@link #SLOW_MS} 안이면 0, 그보다 늦으면 −20
 * - 이야기 고르기 장면에서 잘못 고를 때마다 {@link #WRONG_CHOICE_PENALTY} 점씩,
 *   최대 {@link #MAX_CHOICE_PENALTY} 점까지 깎는다
 * 총점은 0점 아래로 내려가지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationService {

    // 앱이 보내는 재난 종류와 시나리오 제목
    private static final Map<String, String> SCENARIO_TITLES = Map.of(
            "EARTHQUAKE", "지진 대피",
            "FIRE", "화재 대피");

    // 시간 점수. 빨리 하면 더 주고, 너무 늦으면 깎는다
    static final int TIME_BONUS = 20;
    static final int SLOW_PENALTY = 20;
    static final long FAST_MS = 30_000L;
    static final long SLOW_MS = 60_000L;

    // 잘못 고른 한 번에 깎는 점수와 최대로 깎는 점수 (고르기 장면 3개)
    static final int WRONG_CHOICE_PENALTY = 10;
    static final int MAX_CHOICE_PENALTY = 30;

    // 앱 제한 시간보다 넉넉히 잡은 걸린 시간 상한
    private static final long MAX_ELAPSED_MS = 10 * 60 * 1000L;

    private final SimulationMapper simulationMapper;

    private final ObjectMapper objectMapper;

    /** 키워드 하나의 채점 결과. {@code session_results.detail_json} 에 저장한다. */
    private record StepDetail(boolean passed, long elapsedMs) {

    }

    @Transactional
    public SimulationResultResponse saveResult(Long userId, SimulationRequest request) {
        String title = request == null || request.disaster() == null
                ? null : SCENARIO_TITLES.get(request.disaster());
        if (title == null) {
            throw ApiException.badRequest("DISASTER_REQUIRED", "재난 종류가 올바르지 않습니다.");
        }

        Long scenarioId = simulationMapper.selectScenarioIdByTitle(title);
        List<PoseStep> steps = scenarioId == null
                ? List.of() : simulationMapper.selectStepsByScenarioId(scenarioId);
        if (steps.isEmpty()) {
            throw ApiException.notFound("SCENARIO_NOT_FOUND", "준비된 시뮬레이션이 없습니다.");
        }

        boolean success = Boolean.TRUE.equals(request.success());
        long elapsedMs = request.elapsedMs() == null
                ? 0 : Math.max(0, Math.min(request.elapsedMs(), MAX_ELAPSED_MS));
        Set<String> done = request.passedSteps() == null ? Set.of() : Set.copyOf(request.passedSteps());

        long moves = steps.stream().filter(step -> !PoseStep.NAME_QUICK.equals(step.getStepName())).count();
        int timeBonus = moves == steps.size() ? 0 : TIME_BONUS;
        float moveScore = moves == 0 ? 0 : (100f - timeBonus) / moves;

        List<SessionResult> results = new ArrayList<>();
        List<KeywordResponse> keywords = new ArrayList<>();
        float total = 0;

        for (PoseStep step : steps) {
            float score;
            boolean passed;

            if (PoseStep.NAME_QUICK.equals(step.getStepName())) {
                // 시간 키워드는 30초 안에 성공했을 때만 점수를 준다. 늦은 감점은 총점에서 깎는다
                passed = quickPassed(success, elapsedMs);
                score = passed ? timeBonus : 0;
            } else {
                // 성공했으면 모든 동작을 해낸 것이다. 실패했으면 한 번이라도 해낸 동작만 인정한다
                passed = success || done.contains(step.getStepName());
                score = passed ? moveScore : 0;
            }
            total += score;

            results.add(SessionResult.builder()
                    .resultType(SessionResult.TYPE_POSE_STEP)
                    .poseStepId(step.getPoseStepId())
                    .itemOrder(step.getStepOrder())
                    .score(BigDecimal.valueOf(Math.round(score)))
                    .attempts(1)
                    .detailJson(objectMapper.writeValueAsString(new StepDetail(passed, elapsedMs)))
                    .build());
            keywords.add(new KeywordResponse(step.getLabel(), passed,
                    passed ? null : step.getWeakFeedback()));
        }

        int score = Math.max(0, Math.round(total) - slowPenalty(success, elapsedMs)
                - choicePenalty(request.wrongChoices()));
        TrainingSession session = TrainingSession.builder()
                .userId(userId)
                .trainingType(TrainingSession.TYPE_POSE)
                .poseScenarioId(scenarioId)
                .score(BigDecimal.valueOf(score))
                .build();
        simulationMapper.insertSession(session, elapsedMs / 1000);

        for (SessionResult result : results) {
            result.setSessionId(session.getSessionId());
            simulationMapper.insertResult(result);
        }

        log.info("시뮬레이션 결과 저장: userId={}, sessionId={}, disaster={}, score={}, wrongChoices={}",
                userId, session.getSessionId(), request.disaster(), score, request.wrongChoices());

        return new SimulationResultResponse(session.getSessionId(), score, keywords);
    }

    /** 잘못 고른 횟수만큼 깎을 점수 */
    static int choicePenalty(Integer wrongChoices) {
        if (wrongChoices == null || wrongChoices <= 0) {
            return 0;
        }
        return Math.min(MAX_CHOICE_PENALTY, wrongChoices * WRONG_CHOICE_PENALTY);
    }

    /** 30초 안에 성공해야 빠른 반응으로 본다 */
    static boolean quickPassed(boolean success, long elapsedMs) {
        return success && elapsedMs <= FAST_MS;
    }

    /** 60초가 지나서 성공했으면 깎는 점수. 끝내 못 했을 때는 해낸 동작만 받으므로 따로 깎지 않는다 */
    static int slowPenalty(boolean success, long elapsedMs) {
        return success && elapsedMs > SLOW_MS ? SLOW_PENALTY : 0;
    }
}
