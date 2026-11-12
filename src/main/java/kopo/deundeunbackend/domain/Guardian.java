package kopo.deundeunbackend.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code guardians} 테이블 매핑 객체
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Guardian {

    // 보호자 번호 (PK)
    private Long guardianId;

    // 회원 번호
    private Long userId;

    // 보호자 이름
    private String guardianName;

    // 보호자 전화번호
    private String guardianPhone;

    // 관계 (MOTHER, FATHER, PARENT, SIBLING, SPOUSE, TEACHER, SOCIAL_WORKER, ETC)
    private String guardianRelation;

    // 연락 우선순위. 작을수록 먼저
    private Integer guardianPriority;

    // 알림 수신 여부
    private Boolean isNotify;

    // 등록 일시
    private LocalDateTime createdAt;
}
