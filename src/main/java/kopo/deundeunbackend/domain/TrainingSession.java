package kopo.deundeunbackend.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code training_sessions} 테이블 매핑 객체. 훈련 1회
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSession {

    public static final String TYPE_CALL119 = "CALL119";

    // 세션 번호 (PK)
    private Long sessionId;

    // 회원 번호
    private Long userId;

    // 훈련 종류
    private String trainingType;

    // 신고 시나리오 번호
    private Long callScenarioId;

    // 시작일시
    private LocalDateTime startedAt;

    // 종료일시. 진행 중이면 null
    private LocalDateTime endedAt;

    // 총점
    private BigDecimal score;
}
