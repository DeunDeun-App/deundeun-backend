package kopo.deundeunbackend.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 기록 화면에 보여줄 끝난 훈련 1회. {@code training_sessions} 와 시나리오 제목을 묶은 조회 결과 */
@Getter
@Setter
@NoArgsConstructor
public class RecordSession {

    private Long sessionId;

    // CALL119, POSE
    private String trainingType;

    // 시나리오 제목 (화재 신고, 지진 대피 ...)
    private String scenarioTitle;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private BigDecimal score;
}
