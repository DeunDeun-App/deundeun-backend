package kopo.deundeunbackend.dto.response;

import java.time.LocalDateTime;
import kopo.deundeunbackend.domain.Notification;

public record NotificationResponse(String type, String title, String body, LocalDateTime createdAt) {

    public static NotificationResponse from(Notification notification) {
        String title = notification.getTitle() == null || notification.getTitle().isBlank()
                ? defaultTitle(notification.getNotiType())
                : notification.getTitle();

        return new NotificationResponse(notification.getNotiType(), title, notification.getBody(),
                notification.getCreatedAt());
    }

    private static String defaultTitle(String type) {
        if (type == null) {
            return "알림";
        }
        return switch (type) {
            case "SOS" -> "SOS 알림";
            case "LOCATION_SHARE" -> "위치 공유";
            case "TRAINING_RESULT" -> "훈련 완료";
            case "WEEKLY_REPORT" -> "주간 리포트";
            default -> "알림";
        };
    }
}
