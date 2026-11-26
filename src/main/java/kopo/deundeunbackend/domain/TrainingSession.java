package kopo.deundeunbackend.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class TrainingSession {

    public static final String TYPE_CALL119 = "CALL119";

    public static final String TYPE_POSE = "POSE";

    private Long sessionId;

    private Long userId;

    private String trainingType;

    private Long poseScenarioId;

    private Long callScenarioId;

    private LocalDateTime startedAt;

    // 종료일시. 진행 중이면 null
    private LocalDateTime endedAt;

    private BigDecimal score;
}
