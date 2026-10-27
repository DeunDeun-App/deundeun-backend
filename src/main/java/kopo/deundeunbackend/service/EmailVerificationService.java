package kopo.deundeunbackend.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import kopo.deundeunbackend.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    public enum Purpose {
        SIGNUP("회원가입"), PASSWORD_RESET("비밀번호 찾기");

        private final String displayName;

        Purpose(String displayName) {
            this.displayName = displayName;
        }
    }

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(30);
    private static final int MAX_ATTEMPTS = 5;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final MailService mailService;

    private final Map<String, Verification> verifications = new ConcurrentHashMap<>();

    public void sendCode(Purpose purpose, String email) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));

        verifications.put(key(purpose, email),
                new Verification(code, Instant.now().plus(CODE_TTL), false, 0));
        mailService.sendVerificationCode(email, code, purpose.displayName);
    }

    public void verify(Purpose purpose, String email, String code) {
        String key = key(purpose, email);
        Verification verification = verifications.get(key);

        if (verification == null) {
            throw ApiException.badRequest("CODE_NOT_REQUESTED", "인증번호를 먼저 받아 주세요.");
        }
        if (verification.isExpired()) {
            verifications.remove(key);
            throw ApiException.badRequest("CODE_EXPIRED", "인증번호 시간이 지났어요. 다시 받아 주세요.");
        }
        if (verification.attempts() >= MAX_ATTEMPTS) {
            verifications.remove(key);
            throw ApiException.badRequest("CODE_ATTEMPTS_EXCEEDED", "인증번호를 5번 틀렸어요. 다시 받아 주세요.");
        }
        if (!verification.code().equals(code)) {
            verifications.put(key, verification.failed());
            throw ApiException.badRequest("CODE_MISMATCH", "인증번호가 틀렸어요.");
        }

        verifications.put(key, verification.passed(Instant.now().plus(VERIFIED_TTL)));
    }

    public void requireVerified(Purpose purpose, String email) {
        Verification verification = verifications.get(key(purpose, email));

        if (verification == null || !verification.verified() || verification.isExpired()) {
            throw ApiException.badRequest("EMAIL_NOT_VERIFIED", "이메일 인증을 먼저 해 주세요.");
        }
    }

    public void consume(Purpose purpose, String email) {
        verifications.remove(key(purpose, email));
    }

    private String key(Purpose purpose, String email) {
        return purpose + ":" + email.toLowerCase(Locale.ROOT);
    }

    private record Verification(String code, Instant expiresAt, boolean verified, int attempts) {

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }

        Verification failed() {
            return new Verification(code, expiresAt, false, attempts + 1);
        }

        Verification passed(Instant verifiedUntil) {
            return new Verification(code, verifiedUntil, true, attempts);
        }
    }
}
