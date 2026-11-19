package kopo.deundeunbackend.domain;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code session_results} 테이블 매핑 객체. 훈련 1회 안의 항목별 결과
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResult {

    public static final String TYPE_CALL_ITEM = "CALL_ITEM";

    public static final String TYPE_POSE_STEP = "POSE_STEP";

    // 결과 번호 (PK)
    private Long resultId;

    // 세션 번호
    private Long sessionId;

    // 결과 구분
    private String resultType;

    // 자세 단계 번호 (재난 시뮬레이션)
    private Long poseStepId;

    // 신고 항목 번호
    private Long callItemId;

    // 항목 순서
    private Integer itemOrder;

    // 항목 점수. 신고 연습은 통과 100, 실패 0. 시뮬레이션은 받은 점수
    private BigDecimal score;

    // 답한 횟수
    private Integer attempts;

    // 사용자가 한 말 목록 (JSON)
    private String detailJson;

    public boolean isPassed() {
        return score != null && score.signum() > 0;
    }
}
