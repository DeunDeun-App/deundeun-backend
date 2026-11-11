package kopo.deundeunbackend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code call_scenarios} 테이블 매핑 객체
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallScenario {

    // 시나리오 번호 (PK)
    private Long callScenarioId;

    // 제목
    private String callTitle;

    // 상황 설명
    private String situationText;
}
