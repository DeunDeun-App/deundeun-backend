package kopo.deundeunbackend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code call_simulator} 테이블 매핑 객체. 시나리오 안의 질문 하나
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallItem {

    // 항목 번호 (PK)
    private Long itemId;

    // 시나리오 번호
    private Long callScenarioId;

    // 질문 순서
    private Integer itemOrder;

    // 항목 구분 (LOCATION, SITUATION, INJURY ...)
    private String itemType;

    // 완료 화면 체크리스트에 보여줄 이름
    private String label;

    // 기록 화면 통계에 보여줄 이름
    private String statLabel;

    // 상담원 질문
    private String question;

    // 정답 예시 문장
    private String answer;

    // 통과 키워드 (JSON 배열)
    private String keywords;

    // 실패했을 때 보여줄 힌트
    private String weakFeedback;

    // 배점. 비어 있으면 모든 항목을 같은 비중으로 계산
    private Integer score;
}
