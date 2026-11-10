package kopo.deundeunbackend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import kopo.deundeunbackend.common.exception.ApiException;
import kopo.deundeunbackend.common.utils.InputValidator;
import kopo.deundeunbackend.domain.Guardian;
import kopo.deundeunbackend.domain.Notification;
import kopo.deundeunbackend.domain.SosEvent;
import kopo.deundeunbackend.dto.request.SosRequest;
import kopo.deundeunbackend.dto.response.GuardianResponse;
import kopo.deundeunbackend.dto.response.SosResponse;
import kopo.deundeunbackend.mapper.SosMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * SOS 도움 요청 업무 로직.
 *
 * 문자는 서버가 직접 보내지 않는다. 앱이 보호자 번호와 본문이 채워진 문자앱을 열고,
 * 서버는 요청 내용과 보호자 알림 내역만 남긴다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SosService {

    private static final Set<String> SITUATION_CODES = Set.of("FIRE", "EARTHQUAKE", "FLOOD", "UNKNOWN");
    private static final int MESSAGE_MAX_LENGTH = 1000;
    private static final String NOTIFICATION_TITLE = "도움 요청";

    private static final BigDecimal LAT_LIMIT = BigDecimal.valueOf(90);
    private static final BigDecimal LNG_LIMIT = BigDecimal.valueOf(180);

    private final SosMapper sosMapper;

    private final SessionService sessionService;

    private final ObjectMapper objectMapper;

    /** 상태 질문 답. {@code sos_events.checklist_json} 에 저장한다. */
    private record SosChecklist(String situation, boolean trapped, boolean hurt) {

    }

    /**
     * SOS 를 받을 보호자 조회
     *
     * @param authorization 로그인 토큰 ("Bearer ...")
     * @return 알림을 받는 보호자 목록 (우선순위 순)
     */
    @Transactional(readOnly = true)
    public List<GuardianResponse> getGuardians(String authorization) {
        return requireGuardians(sessionService.requireUserId(authorization)).stream()
                .map(GuardianResponse::from)
                .toList();
    }

    /**
     * 도움 요청 저장. 보호자마다 대기 상태 알림을 만든다
     *
     * @param authorization 로그인 토큰 ("Bearer ...")
     * @param request       위치, 상태 질문 답, 문자 본문
     * @return SOS 번호와 문자를 받을 전화번호
     */
    @Transactional
    public SosResponse request(String authorization, SosRequest request) {
        Long userId = sessionService.requireUserId(authorization);
        if (request == null) {
            throw ApiException.badRequest("SITUATION_INVALID", "어떤 일이 생겼는지 골라 주세요.");
        }
        List<Guardian> guardians = requireGuardians(userId);

        String checklistJson = toChecklistJson(request);
        validateLocation(request.lat(), request.lng());

        String message = InputValidator.trimToNull(request.message());
        if (message == null) {
            throw ApiException.badRequest("MESSAGE_REQUIRED", "보낼 메시지가 없습니다.");
        }
        if (message.length() > MESSAGE_MAX_LENGTH) {
            throw ApiException.badRequest("MESSAGE_TOO_LONG", "메시지가 너무 깁니다.");
        }

        SosEvent sosEvent = SosEvent.builder()
                .userId(userId)
                .lat(request.lat())
                .lng(request.lng())
                .checklistJson(checklistJson)
                .sendMsg(message)
                .build();
        sosMapper.insertSosEvent(sosEvent);

        for (Guardian guardian : guardians) {
            sosMapper.insertNotification(Notification.builder()
                    .userId(userId)
                    .guardianId(guardian.getGuardianId())
                    .sosId(sosEvent.getSosId())
                    .notiType(Notification.TYPE_SOS)
                    .channel(Notification.CHANNEL_SMS)
                    .title(NOTIFICATION_TITLE)
                    .body(message)
                    .build());
        }

        log.info("SOS 요청: userId={}, sosId={}, guardians={}, hasLocation={}",
                userId, sosEvent.getSosId(), guardians.size(), request.lat() != null);

        List<String> phones = guardians.stream().map(Guardian::getGuardianPhone).toList();
        return new SosResponse(sosEvent.getSosId(), phones);
    }

    /**
     * 문자앱으로 넘겼음을 기록. 같은 요청을 여러 번 보내도 처음 시각만 남는다
     *
     * @param authorization 로그인 토큰 ("Bearer ...")
     * @param sosId         SOS 번호
     */
    @Transactional
    public void markSent(String authorization, Long sosId) {
        Long userId = sessionService.requireUserId(authorization);
        SosEvent sosEvent = sosMapper.selectSosEvent(sosId);

        // 다른 회원의 요청도 없는 것으로 본다
        if (sosEvent == null || !sosEvent.getUserId().equals(userId)) {
            throw ApiException.notFound("SOS_NOT_FOUND", "도움 요청 내역을 찾을 수 없습니다.");
        }

        sosMapper.updateSendAt(sosId);
        int updated = sosMapper.updateNotificationsSent(sosId);
        log.info("SOS 문자앱 전달: userId={}, sosId={}, notifications={}", userId, sosId, updated);
    }


    private List<Guardian> requireGuardians(Long userId) {
        Boolean sosAlert = sosMapper.selectSosAlert(userId);

        if (sosAlert == null) {
            throw ApiException.notFound("USER_NOT_FOUND", "회원 정보를 찾을 수 없습니다.");
        }
        if (!sosAlert) {
            throw ApiException.conflict("SOS_ALERT_OFF", "보호자 알림이 꺼져 있어요. 내 정보에서 켜 주세요.");
        }

        List<Guardian> guardians = sosMapper.selectNotifyGuardians(userId);

        if (guardians.isEmpty()) {
            throw ApiException.notFound("GUARDIAN_NOT_FOUND", "등록된 보호자가 없어요. 내 정보에서 보호자를 등록해 주세요.");
        }
        return guardians;
    }

    private String toChecklistJson(SosRequest request) {
        if (request.situation() == null || !SITUATION_CODES.contains(request.situation())) {
            throw ApiException.badRequest("SITUATION_INVALID", "어떤 일이 생겼는지 골라 주세요.");
        }
        if (request.trapped() == null || request.hurt() == null) {
            throw ApiException.badRequest("CHECKLIST_REQUIRED", "질문에 모두 답해 주세요.");
        }

        try {
            return objectMapper.writeValueAsString(
                    new SosChecklist(request.situation(), request.trapped(), request.hurt()));
        } catch (JacksonException e) {
            log.error("SOS 체크리스트 변환 실패", e);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "SOS_SAVE_FAILED",
                    "잠시 후 다시 시도해 주세요.");
        }
    }

    // 위치는 둘 다 있거나 둘 다 없어야 한다
    private void validateLocation(BigDecimal lat, BigDecimal lng) {
        if (lat == null && lng == null) {
            return;
        }
        if (lat == null || lng == null
                || lat.abs().compareTo(LAT_LIMIT) > 0
                || lng.abs().compareTo(LNG_LIMIT) > 0) {
            throw ApiException.badRequest("LOCATION_INVALID", "위치 정보가 올바르지 않습니다.");
        }
    }
}
