package kopo.deundeunbackend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kopo.deundeunbackend.common.exception.ApiException;
import kopo.deundeunbackend.common.utils.InputValidator;
import kopo.deundeunbackend.common.utils.KeywordRule;
import kopo.deundeunbackend.domain.CallItem;
import kopo.deundeunbackend.domain.CallScenario;
import kopo.deundeunbackend.domain.SessionResult;
import kopo.deundeunbackend.domain.TrainingSession;
import kopo.deundeunbackend.dto.request.CallRequest;
import kopo.deundeunbackend.dto.response.CallAnswerResponse;
import kopo.deundeunbackend.dto.response.CallItemResponse;
import kopo.deundeunbackend.dto.response.CallItemResultResponse;
import kopo.deundeunbackend.dto.response.CallResultResponse;
import kopo.deundeunbackend.dto.response.CallSessionResponse;
import kopo.deundeunbackend.mapper.CallMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/** 119 신고 연습 업무 로직. */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallPracticeService {

    // 질문 하나에 답할 수 있는 횟수. 다 틀리면 실패로 기록하고 다음 질문으로 넘어간다
    private static final int MAX_ATTEMPTS = 2;

    // 주소 질문. 기회 제한 없이 말할 때까지 묻는다
    private static final String ITEM_TYPE_LOCATION = "LOCATION";
    private static final int ANSWER_MAX_LENGTH = 500;

    private static final BigDecimal PASSED_SCORE = BigDecimal.valueOf(100);

    private final CallMapper callMapper;

    private final SessionService sessionService;

    private final ObjectMapper objectMapper;

    /** 사용자가 지금까지 한 말. {@code session_results.detail_json} 에 저장한다. */
    private record CallDetail(List<String> answers) {

    }

    /**
     * 신고 연습 시작
     *
     * @param authorization 로그인 토큰 ("Bearer ...")
     * @return 세션 번호와 상담원 질문 목록
     */
    @Transactional
    public CallSessionResponse startSession(String authorization) {
        Long userId = sessionService.requireUserId(authorization);

        if (!callMapper.existsUser(userId)) {
            throw ApiException.notFound("USER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
        }

        CallScenario scenario = callMapper.selectRandomScenario();

        if (scenario == null) {
            throw ApiException.notFound("SCENARIO_NOT_FOUND", "준비된 신고 연습이 없습니다.");
        }

        TrainingSession session = TrainingSession.builder()
                .userId(userId)
                .trainingType(TrainingSession.TYPE_CALL119)
                .callScenarioId(scenario.getCallScenarioId())
                .build();

        callMapper.insertSession(session);
        log.info("신고 연습 시작: userId={}, sessionId={}, scenarioId={}",
                userId, session.getSessionId(), scenario.getCallScenarioId());

        List<CallItemResponse> items = callMapper.selectItemsByScenarioId(scenario.getCallScenarioId())
                .stream()
                .map(CallItemResponse::from)
                .toList();

        return new CallSessionResponse(session.getSessionId(), scenario.getCallTitle(),
                scenario.getSituationText(), items);
    }

    /**
     * 상담원 질문에 대한 답변을 판정하고 저장
     *
     * @param authorization 로그인 토큰 ("Bearer ...")
     * @param sessionId     세션 번호
     * @param request       질문 번호, 음성 인식 결과
     * @return 판정 결과
     */
    @Transactional
    public CallAnswerResponse answer(String authorization, Long sessionId, CallRequest request) {
        TrainingSession session = requireOpenSession(sessionId, sessionService.requireUserId(authorization));

        if (request == null || request.itemId() == null) {
            throw ApiException.badRequest("ITEM_REQUIRED", "질문 정보가 없습니다.");
        }

        CallItem item = callMapper.selectItem(request.itemId());

        if (item == null || !item.getCallScenarioId().equals(session.getCallScenarioId())) {
            throw ApiException.notFound("ITEM_NOT_FOUND", "질문을 찾을 수 없습니다.");
        }

        String answer = InputValidator.trimToNull(request.answer());

        if (answer == null) {
            throw ApiException.badRequest("ANSWER_REQUIRED", "잘 안 들렸어요. 다시 말해 주세요.");
        }
        if (answer.length() > ANSWER_MAX_LENGTH) {
            throw ApiException.badRequest("ANSWER_TOO_LONG", "조금 짧게 말해 주세요.");
        }

        SessionResult result = callMapper.selectResult(sessionId, item.getItemId());
        KeywordRule rule = readRule(item);
        List<String> previous = result == null ? List.of() : readAnswers(result.getDetailJson());

        if (result != null && (result.isPassed() || !canAnswerAgain(item, rule, previous))) {
            throw ApiException.conflict("ITEM_ALREADY_JUDGED", "이미 답한 질문입니다.");
        }

        List<String> answers = new ArrayList<>(previous);
        answers.add(answer);

        KeywordRule.Judgement judgement = rule.judge(answers);
        boolean passed = judgement.passed();
        BigDecimal score = passed ? PASSED_SCORE : BigDecimal.ZERO;

        if (result == null) {
            result = SessionResult.builder()
                    .sessionId(sessionId)
                    .resultType(SessionResult.TYPE_CALL_ITEM)
                    .callItemId(item.getItemId())
                    .itemOrder(item.getItemOrder())
                    .score(score)
                    .attempts(1)
                    .detailJson(writeDetail(null, answer))
                    .build();

            callMapper.insertResult(result);

        } else {
            result.setScore(score);
            result.setAttempts(result.getAttempts() + 1);
            result.setDetailJson(writeDetail(result.getDetailJson(), answer));

            callMapper.updateResult(result);
        }

        boolean retryable = !passed && canAnswerAgain(item, rule, answers);

        // 단서를 일부만 말했으면 빠진 단서를 되묻는다. 아무것도 못 말했으면 도움말을 보여준다
        String followUp = null;
        if (retryable && judgement.matched() > 0) {
            followUp = judgement.missing().stream()
                    .map(KeywordRule.Part::ask)
                    .filter(ask -> ask != null && !ask.isBlank())
                    .findFirst()
                    .orElse(null);
        }

        return new CallAnswerResponse(passed, result.getAttempts(), retryable,
                passed || followUp != null ? null : item.getWeakFeedback(), followUp);
    }

    /**
     * 한 번 더 답할 수 있는지
     *
     * 새 단서를 말하지 못한 답변만 기회를 쓴 것으로 센다. 새 단서를 말하면 기회가 줄지 않는다.
     * 단서가 많아도 끝없이 묻지 않도록 전체 답변 수는 (단서 수 + 기회 수)로 제한한다.
     * 주소는 구조대가 찾아가는 데 꼭 필요하므로 말할 때까지 계속 묻는다.
     */
    private boolean canAnswerAgain(CallItem item, KeywordRule rule, List<String> answers) {
        if (ITEM_TYPE_LOCATION.equals(item.getItemType())) {
            return true;
        }
        int misses = 0;
        int matched = 0;
        for (int i = 0; i < answers.size(); i++) {
            int now = rule.judge(answers.subList(0, i + 1)).matched();
            if (now == matched) {
                misses++;
            }
            matched = now;
        }
        return misses < MAX_ATTEMPTS && answers.size() < rule.parts().size() + MAX_ATTEMPTS;
    }

    /**
     * 신고 연습 종료. 답하지 않은 질문은 실패로 본다
     *
     * @param authorization 로그인 토큰 ("Bearer ...")
     * @param sessionId     세션 번호
     * @return 총점과 항목별 결과
     */
    @Transactional
    public CallResultResponse complete(String authorization, Long sessionId) {
        TrainingSession session = requireOpenSession(sessionId, sessionService.requireUserId(authorization));

        List<CallItem> items = callMapper.selectItemsByScenarioId(session.getCallScenarioId());
        Map<Long, SessionResult> resultByItem = callMapper.selectResultsBySessionId(sessionId)
                .stream()
                .collect(Collectors.toMap(SessionResult::getCallItemId, Function.identity()));

        // 배점이 하나라도 비어 있으면 모든 항목을 같은 비중으로 계산
        boolean weighted = !items.isEmpty() && items.stream().allMatch(item -> item.getScore() != null);

        int totalWeight = 0;
        int passedWeight = 0;
        List<CallItemResultResponse> results = new ArrayList<>();

        for (CallItem item : items) {
            SessionResult result = resultByItem.get(item.getItemId());
            boolean passed = result != null && result.isPassed();
            int weight = weighted ? item.getScore() : 1;

            totalWeight += weight;
            if (passed) {
                passedWeight += weight;
            }

            results.add(new CallItemResultResponse(item.getItemId(), item.getLabel(),
                    item.getStatLabel(), passed));
        }

        int score = totalWeight == 0 ? 0 : Math.round(passedWeight * 100f / totalWeight);

        if (callMapper.endSession(sessionId, score) != 1) {
            throw sessionEnded();
        }

        log.info("신고 연습 종료: sessionId={}, score={}", sessionId, score);

        return new CallResultResponse(score, results);
    }

    // 다른 회원의 세션은 없는 세션처럼 처리한다
    private TrainingSession requireOpenSession(Long sessionId, Long userId) {
        TrainingSession session = sessionId == null ? null : callMapper.selectSession(sessionId);

        if (session == null || !TrainingSession.TYPE_CALL119.equals(session.getTrainingType())
                || !session.getUserId().equals(userId)) {
            throw ApiException.notFound("SESSION_NOT_FOUND", "신고 연습 정보를 찾을 수 없습니다.");
        }
        if (session.getEndedAt() != null) {
            throw sessionEnded();
        }

        return session;
    }

    private ApiException sessionEnded() {
        return new ApiException(HttpStatus.CONFLICT, "SESSION_ENDED", "이미 끝난 신고 연습입니다.");
    }

    /** 키워드 JSON 을 통과 규칙으로 읽는다. 배열이면 하나라도, {"parts": [...]} 면 모든 단서를 말해야 통과. */
    private KeywordRule readRule(CallItem item) {
        String json = item.getKeywords();
        try {
            if (json != null && json.trim().startsWith("{")) {
                KeywordRule rule = objectMapper.readValue(json, KeywordRule.class);
                if (rule.parts() != null && !rule.parts().isEmpty()) {
                    return rule;
                }
            } else if (json != null) {
                return KeywordRule.anyOf(List.of(objectMapper.readValue(json, String[].class)));
            }

        } catch (JacksonException e) {
            log.error("통과 키워드 형식 오류: itemId={}", item.getItemId(), e);
        }
        return KeywordRule.anyOf(List.of());
    }

    private List<String> readAnswers(String detailJson) {
        if (detailJson == null) {
            return List.of();
        }
        try {
            List<String> answers = objectMapper.readValue(detailJson, CallDetail.class).answers();
            return answers == null ? List.of() : answers;

        } catch (JacksonException e) {
            log.warn("답변 기록 형식 오류: {}", detailJson);
            return List.of();
        }
    }

    private String writeDetail(String detailJson, String answer) {
        List<String> answers = new ArrayList<>();

        if (detailJson != null) {
            try {
                answers.addAll(objectMapper.readValue(detailJson, CallDetail.class).answers());

            } catch (JacksonException | NullPointerException e) {
                log.warn("답변 기록 형식 오류, 새로 기록합니다: {}", detailJson);
            }
        }
        answers.add(answer);

        return objectMapper.writeValueAsString(new CallDetail(answers));
    }
}
