package kopo.deundeunbackend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import kopo.deundeunbackend.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private static final String VERIFICATION_TEMPLATE = "templates/mail/verification-code.html";

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${deundeun.mail.enabled:false}")
    private boolean enabled;

    @Value("${deundeun.mail.from:}")
    private String from;

    private String verificationTemplate;

    public void sendVerificationCode(String to, String code, String purposeName, long validMinutes) {
        JavaMailSender mailSender = enabled ? mailSenderProvider.getIfAvailable() : null;

        if (mailSender == null) {
            log.warn("메일 서버 설정이 없어 발송을 생략합니다: email={}, code={}", to, code);
            return;
        }

        String html = verificationTemplate()
                .replace("${purpose}", purposeName)
                .replace("${code}", code)
                .replace("${minutes}", String.valueOf(validMinutes));
        String text = "인증 코드: " + code + "\n\n" + validMinutes + "분 안에 입력해 주세요.";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            if (!from.isBlank()) {
                helper.setFrom(from);
            }
            helper.setTo(to);
            helper.setSubject("[든든] " + purposeName + " 이메일 인증 코드");
            helper.setText(text, html);

            mailSender.send(message);
            log.info("인증번호 메일 발송: email={}", to);

        } catch (MailException | MessagingException e) {
            log.error("인증번호 메일 발송 실패: email={}", to, e);
            throw ApiException.unavailable("MAIL_SEND_FAILED", "메일을 보내지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }

    private String verificationTemplate() {
        if (verificationTemplate == null) {
            try {
                verificationTemplate = StreamUtils.copyToString(
                        new ClassPathResource(VERIFICATION_TEMPLATE).getInputStream(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return verificationTemplate;
    }
}
