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
public class CallItem {

    private Long itemId;

    private Long callScenarioId;

    private Integer itemOrder;

    // 항목 구분 (LOCATION, SITUATION, INJURY ...)
    private String itemType;

    // 완료 화면 체크리스트에 보여줄 이름
    private String label;

    // 기록 화면 통계에 보여줄 이름
    private String statLabel;

    private String question;

    private String answer;

    private String keywords;

    private String weakFeedback;

    // 배점. 비어 있으면 모든 항목을 같은 비중으로 계산
    private Integer score;
}
