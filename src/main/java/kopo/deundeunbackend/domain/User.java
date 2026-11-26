package kopo.deundeunbackend.domain;

import java.time.LocalDate;
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
public class User {

    private Long userId;

    private String loginId;

    private String email;

    private String pwHash;

    private String pwSalt;

    private String userName;

    private String userPhone;

    private LocalDate birthDate;

    private String gender;

    private String disabilityType;

    private String severity;

    private String address;

    private Boolean isShareLocation;

    private Boolean isSosAlert;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
