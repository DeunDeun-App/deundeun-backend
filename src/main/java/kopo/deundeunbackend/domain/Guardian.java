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

    private String guardianRelation;

    private Integer guardianPriority;

    private Boolean isNotify;

    private LocalDateTime createdAt;
}
