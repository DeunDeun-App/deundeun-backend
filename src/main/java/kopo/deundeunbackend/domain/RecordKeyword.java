package kopo.deundeunbackend.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 끝난 훈련 1회의 키워드 하나. 신고 연습은 질문, 시뮬레이션은 자세 단계
 *
 * 신고 연습에서 답하지 않은 질문은 결과 행이 없어서 못 한 것으로 조회된다.
 */
@Getter
@Setter
@NoArgsConstructor
public class RecordKeyword {

    private Long sessionId;

    // CALL119, POSE
    private String trainingType;

    private Integer itemOrder;

    // 기록 카드에 보여줄 이름 (주소, 머리 보호 ...)
    private String label;

    // 항목별 달성률에 보여줄 이름 (주소 말하기 ...)
    private String statLabel;

    private String feedback;

    private boolean passed;
}
