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
 * 100점 만점. 빠른 반응이 20점이고, 나머지 80점은 동작 키워드끼리 똑같이 나눈다
 * (지진: 웅크리기 40 + 머리 보호 40, 화재: 코·입 막기 80).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationService {

    // 앱이 보내는 재난 종류와 시나리오 제목
    private static final Map<String, String> SCENARIO_TITLES = Map.of(
            "EARTHQUAKE", "지진 대피",
            "FIRE", "화재 대피");

    private static final int QUICK_SCORE = 20;

    // 앱 제한 시간보다 넉넉히 잡은 걸린 시간 상한
    private static final long MAX_ELAPSED_MS = 10 * 60 * 1000L;

    private final SimulationMapper simulationMapper;

    private final SessionService sessionService;

    private final ObjectMapper objectMapper;

    /** 키워드 하나의 채점 결과. {@code session_results.detail_json} 에 저장한다. */
    private record StepDetail(boolean passed, long elapsedMs) {

    }

    /**
     * 시뮬레이션 1회 결과를 채점하고 저장
     *
     * @param authorization 로그인 토큰 ("Bearer ...")
     * @param request       재난 종류, 성공 여부, 걸린 시간, 해낸 동작
     * @return 총점과 키워드별 결과
     */
    @Transactional
    public SimulationResultResponse saveResult(String authorization, SimulationRequest request) {
        Long userId = sessionService.requireUserId(authorization);

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
        int quickScore = moves == steps.size() ? 0 : QUICK_SCORE;
        float moveScore = moves == 0 ? 0 : (100f - quickScore) / moves;

        List<SessionResult> results = new ArrayList<>();
        List<KeywordResponse> keywords = new ArrayList<>();
        float total = 0;

        for (PoseStep step : steps) {
            float score;
            boolean passed;

            if (PoseStep.NAME_QUICK.equals(step.getStepName())) {
                score = quickScore * quickRatio(success, elapsedMs);
                passed = quickPassed(success, elapsedMs);
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

        int score = Math.round(total);
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

        log.info("시뮬레이션 결과 저장: userId={}, sessionId={}, disaster={}, score={}",
                userId, session.getSessionId(), request.disaster(), score);

        return new SimulationResultResponse(session.getSessionId(), score, keywords);
    }

    /** 빠른 반응 배점 중 받는 비율. 10초 안 전부, 30초 안 75%, 그 뒤 성공 50%, 실패 0 */
    static float quickRatio(boolean success, long elapsedMs) {
        if (!success) {
            return 0f;
        }
        if (elapsedMs <= 10_000L) {
            return 1f;
        }
        return elapsedMs <= 30_000L ? 0.75f : 0.5f;
    }

    /** 30초 안에 성공해야 빠른 반응으로 본다 */
    static boolean quickPassed(boolean success, long elapsedMs) {
        return success && elapsedMs <= 30_000L;
    }
}
