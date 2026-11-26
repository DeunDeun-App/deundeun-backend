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
public class Guardian {

    private Long guardianId;

    private Long userId;

    private String guardianName;

    private String guardianPhone;

    // 관계 (MOTHER, FATHER, PARENT, SIBLING, SPOUSE, TEACHER, SOCIAL_WORKER, ETC)
    private String guardianRelation;

    // 연락 우선순위. 작을수록 먼저
    private Integer guardianPriority;

    private Boolean isNotify;

    private LocalDateTime createdAt;
}
