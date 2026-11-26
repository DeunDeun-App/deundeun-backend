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
public class SosEvent {

    private Long sosId;

    private Long userId;

    // 위도. 위치를 못 받으면 null
    private BigDecimal lat;

    // 경도. 위치를 못 받으면 null
    private BigDecimal lng;

    private String checklistJson;

    private String sendMsg;

    // 문자앱으로 넘긴 일시. 넘기기 전이면 null
    private LocalDateTime sendAt;
}
