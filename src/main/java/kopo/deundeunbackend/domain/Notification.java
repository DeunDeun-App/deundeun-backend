package kopo.deundeunbackend.domain;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Notification {

    private Long notiId;

    private String notiType;

    private String title;

    private String body;

    private String status;

    private LocalDateTime createdAt;
}
