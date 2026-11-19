package kopo.deundeunbackend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code pose_steps} 테이블 매핑 객체. 재난 시뮬레이션에서 채점하는 키워드 하나
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoseStep {

    // 시간 안에 자세를 잡았는지 보는 키워드. 앱이 보낸 걸린 시간으로 채점한다
    public static final String NAME_QUICK = "QUICK";

    // 단계 번호 (PK)
    private Long poseStepId;

    // 시나리오 번호
    private Long poseScenarioId;

    // 순서
    private Integer stepOrder;

    // 앱이 보내는 키워드 이름 (CROUCH, COVER_HEAD, COVER_MOUTH, QUICK)
    private String stepName;

    // 완료 화면·기록에 보여줄 이름
    private String label;

    // 못 했을 때 보여줄 피드백
    private String weakFeedback;
}
