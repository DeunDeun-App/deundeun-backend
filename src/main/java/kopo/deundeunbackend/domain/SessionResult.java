package kopo.deundeunbackend.domain;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResult {

    public static final String TYPE_CALL_ITEM = "CALL_ITEM";

    public static final String TYPE_POSE_STEP = "POSE_STEP";

    private Long resultId;

    private Long sessionId;

    private String resultType;

    private Long poseStepId;

    private Long callItemId;

    private Integer itemOrder;

    // 항목 점수. 신고 연습은 통과 100, 실패 0. 시뮬레이션은 받은 점수
    private BigDecimal score;

    private Integer attempts;

    private String detailJson;

    public boolean isPassed() {
        return score != null && score.signum() > 0;
    }
}
