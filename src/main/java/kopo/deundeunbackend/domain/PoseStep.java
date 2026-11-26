package kopo.deundeunbackend.domain;

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
public class PoseStep {

    // 시간 안에 자세를 잡았는지 보는 키워드. 앱이 보낸 걸린 시간으로 채점한다
    public static final String NAME_QUICK = "QUICK";

    private Long poseStepId;

    private Long poseScenarioId;

    private Integer stepOrder;

    // 앱이 보내는 키워드 이름 (CROUCH, COVER_HEAD, COVER_MOUTH, QUICK)
    private String stepName;

    // 완료 화면·기록에 보여줄 이름
    private String label;

    private String weakFeedback;
}
