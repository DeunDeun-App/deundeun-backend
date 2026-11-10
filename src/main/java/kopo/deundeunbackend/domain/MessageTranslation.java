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
public class MessageTranslation {

    private Long translationId;

    private String msgHash;

    private String disasterCode;

    private String region;

    private String originalText;

    private String easyText;

    private String ttsText;

    private String actionText;

    private String pictogramCodes;

    private LocalDateTime receivedAt;
}
