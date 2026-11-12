package kopo.deundeunbackend.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code notifications} 테이블 매핑 객체
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    public static final String TYPE_SOS = "SOS";
    public static final String CHANNEL_SMS = "SMS";

    // 알림 번호 (PK)
    private Long notiId;

    // 회원 번호
    private Long userId;

    // 보호자 번호
    private Long guardianId;

    // SOS 번호
    private Long sosId;

    // 알림 종류 (SOS, LOCATION_SHARE, TRAINING_RESULT, WEEKLY_REPORT, ETC)
    private String notiType;

    // 발송 수단 (SMS, FCM)
    private String channel;

    // 제목
    private String title;

    // 내용
    private String body;

    // 발송 상태
    private String status;

    // 생성 일시
    private LocalDateTime createdAt;
}
