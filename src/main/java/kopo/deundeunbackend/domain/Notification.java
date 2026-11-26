package kopo.deundeunbackend.domain;

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
public class Notification {

    public static final String TYPE_SOS = "SOS";
    public static final String CHANNEL_SMS = "SMS";

    private Long notiId;

    private Long userId;

    private Long guardianId;

    private Long sosId;

    // 알림 종류 (SOS, LOCATION_SHARE, TRAINING_RESULT, WEEKLY_REPORT, ETC)
    private String notiType;

    // 발송 수단 (SMS, FCM)
    private String channel;

    private String title;

    private String body;

    private String status;

    private LocalDateTime createdAt;
}
