package kopo.deundeunbackend.mapper;

import java.util.List;
import kopo.deundeunbackend.domain.Guardian;
import kopo.deundeunbackend.domain.Notification;
import kopo.deundeunbackend.domain.SosEvent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SosMapper {

    /**
     * 회원의 SOS 알림 설정 조회
     *
     * @param userId 회원 번호
     * @return SOS 알림 수신 여부. 회원이 없으면 null
     */
    Boolean selectSosAlert(@Param("userId") Long userId);

    /**
     * 알림을 받는 보호자 목록 (우선순위 순)
     *
     * @param userId 회원 번호
     * @return 보호자 목록
     */
    List<Guardian> selectNotifyGuardians(@Param("userId") Long userId);

    /**
     * SOS 이력 등록
     *
     * @param sosEvent 등록할 SOS. 등록 후 {@code sosId} 가 채워진다
     * @return 등록된 행 수
     */
    int insertSosEvent(SosEvent sosEvent);

    /**
     * SOS 이력 조회
     *
     * @param sosId SOS 번호
     * @return SOS. 없으면 null
     */
    SosEvent selectSosEvent(@Param("sosId") Long sosId);

    /**
     * 보호자 알림 등록. 상태는 PENDING 으로 시작한다
     *
     * @param notification 등록할 알림
     * @return 등록된 행 수
     */
    int insertNotification(Notification notification);

    /**
     * 문자앱으로 넘긴 시각 기록
     *
     * @param sosId SOS 번호
     * @return 변경된 행 수. 이미 기록돼 있으면 0
     */
    int updateSendAt(@Param("sosId") Long sosId);

    /**
     * 해당 SOS 의 대기 중 알림을 발송으로 변경
     *
     * @param sosId SOS 번호
     * @return 변경된 행 수
     */
    int updateNotificationsSent(@Param("sosId") Long sosId);
}
