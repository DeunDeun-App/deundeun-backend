package kopo.deundeunbackend.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code users} 테이블 매핑 객체
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    // 회원 아이디 (PK)
    private Long userId;

    // 이메일
    private String email;

    // 비밀번호 해시
    private String pwHash;

    // 솔트
    private String pwSalt;

    // 회원 이름
    private String userName;

    // 회원 전화번호
    private String userPhone;

    // 생년월일
    private LocalDate birthDate;

    // 성별
    private String gender;

    // 장애 정도
    private String severity;

    // 집주소
    private String address;

    // 훈련 결과 공유 여부
    private Boolean isShareResult;

    // SOS 알림 수신 여부
    private Boolean isSosAlert;

    // 가입일시
    private LocalDateTime createdAt;

    // 수정일시
    private LocalDateTime updatedAt;
}
