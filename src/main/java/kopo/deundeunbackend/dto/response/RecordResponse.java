package kopo.deundeunbackend.dto.response;

import java.util.List;

/**
 * 기록 화면 전체
 *
 * @param weekLabels       최근 5주 라벨 (각 주 월요일, "11/10")
 * @param simulationCounts 주별 시뮬레이션 횟수 (마지막 칸이 이번 주)
 * @param callCounts       주별 119 신고 연습 횟수 (마지막 칸이 이번 주)
 * @param dayMinutes       이번 주 월~일 요일별 훈련 시간 (분, 7칸)
 * @param records          최근 훈련 (최신순)
 * @param call             119 신고 연습 점수 변화와 달성률
 * @param simulation       재난 시뮬레이션 점수 변화와 달성률
 */
public record RecordResponse(List<String> weekLabels, List<Integer> simulationCounts,
                             List<Integer> callCounts, List<Integer> dayMinutes, List<Item> records,
                             Report call, Report simulation) {

    /**
     * 훈련 1회
     *
     * @param trainingType CALL119, POSE
     * @param title        화면에 보여줄 훈련 이름 (119 신고 연습, 재난 시뮬레이션)
     * @param scenario     시나리오 제목 (화재 신고, 지진 대피 ...)
     * @param performedAt  시작 시각 ("2026-11-16T10:05")
     */
    public record Item(Long sessionId, String trainingType, String title, String scenario,
                       String performedAt, int score, List<KeywordResponse> keywords) {

    }

    /**
     * 훈련 종류 하나의 리포트
     *
     * @param scoreLabels 최근 연습 날짜 ("11/16"), 오래된 것부터
     * @param scores      최근 연습 점수, 오래된 것부터
     * @param rates       항목별 달성률
     */
    public record Report(List<String> scoreLabels, List<Integer> scores, List<Rate> rates) {

    }

    /** 항목별 달성률. 전체 연습 중 해낸 비율 (0~100) */
    public record Rate(String label, int percent) {

    }
}
