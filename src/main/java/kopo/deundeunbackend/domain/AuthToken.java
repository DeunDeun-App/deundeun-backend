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
public class AuthToken {

    public static final String TYPE_PASSWORD_RESET = "PASSWORD_RESET";

    private Long tokenId;

    private Long userId;

    private String tokenType;

    private String tokenValue;

    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;

    private LocalDateTime createdAt;
}
