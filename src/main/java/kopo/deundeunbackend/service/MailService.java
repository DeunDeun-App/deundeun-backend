package kopo.deundeunbackend.service;

import kopo.deundeunbackend.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${deundeun.mail.enabled:false}")
    private boolean enabled;

    @Value("${deundeun.mail.from:}")
    private String from;

    public void sendVerificationCode(String to, String code, String purposeName) {
        JavaMailSender mailSender = enabled ? mailSenderProvider.getIfAvailable() : null;

        if (mailSender == null) {
            log.warn("메일 서버 설정이 없어 발송을 생략합니다: email={}, code={}", to, code);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (!from.isBlank()) {
            message.setFrom(from);
        }
        message.setTo(to);
        message.setSubject("[든든] " + purposeName + " 이메일 인증 코드");
        message.setText("인증 코드: " + code + "\n\n5분 안에 입력해 주세요.");

        try {
            mailSender.send(message);
            log.info("인증번호 메일 발송: email={}", to);

        } catch (MailException e) {
            log.error("인증번호 메일 발송 실패: email={}", to, e);
            throw ApiException.unavailable("MAIL_SEND_FAILED", "메일을 보내지 못했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }
}
