package kopo.deundeunbackend.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code sos_events} 테이블 매핑 객체
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SosEvent {

    // SOS 번호 (PK)
    private Long sosId;

    // 회원 번호
    private Long userId;

    // 위도. 위치를 못 받으면 null
    private BigDecimal lat;

    // 경도. 위치를 못 받으면 null
    private BigDecimal lng;

    // 상태 질문 답 (JSON)
    private String checklistJson;

    // 보호자에게 보낸 문자 본문
    private String sendMsg;

    // 문자앱으로 넘긴 일시. 넘기기 전이면 null
    private LocalDateTime sendAt;
}
