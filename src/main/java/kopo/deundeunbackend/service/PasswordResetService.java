package kopo.deundeunbackend.service;

import java.util.UUID;
import kopo.deundeunbackend.common.exception.ApiException;
import kopo.deundeunbackend.common.utils.InputValidator;
import kopo.deundeunbackend.common.utils.PasswordUtil;
import kopo.deundeunbackend.domain.AuthToken;
import kopo.deundeunbackend.domain.User;
import kopo.deundeunbackend.dto.request.EmailCodeRequest;
import kopo.deundeunbackend.dto.request.EmailVerifyRequest;
import kopo.deundeunbackend.dto.request.PasswordResetRequest;
import kopo.deundeunbackend.dto.request.ResetTokenRequest;
import kopo.deundeunbackend.dto.response.ResetTokenResponse;
import kopo.deundeunbackend.mapper.AuthTokenMapper;
import kopo.deundeunbackend.mapper.UserMapper;
import kopo.deundeunbackend.service.EmailVerificationService.Purpose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final int RESET_TOKEN_VALID_MINUTES = 10;

    private final UserMapper userMapper;

    private final AuthTokenMapper authTokenMapper;

    private final EmailVerificationService emailVerificationService;

    public void sendCode(EmailCodeRequest request) {
        String email = InputValidator.requireEmail(request == null ? null : request.email());

        if (userMapper.selectByEmail(email) == null) {
            throw ApiException.notFound("USER_NOT_FOUND", "가입되지 않은 이메일입니다.");
        }

        emailVerificationService.sendCode(Purpose.PASSWORD_RESET, email);
    }

    public void verifyCode(EmailVerifyRequest request) {
        String email = InputValidator.requireEmail(request == null ? null : request.email());
        String code = InputValidator.requireCode(request.code());

        emailVerificationService.verify(Purpose.PASSWORD_RESET, email, code);
    }

    public ResetTokenResponse issueResetToken(ResetTokenRequest request) {
        String name = InputValidator.requireName(request == null ? null : request.name());
        String email = InputValidator.requireEmail(request.email());

        emailVerificationService.requireVerified(Purpose.PASSWORD_RESET, email);

        User user = userMapper.selectByEmail(email);

        if (user == null || !name.equals(user.getUserName())) {
            throw ApiException.notFound("USER_NOT_FOUND", "입력한 이름과 이메일이 일치하는 회원이 없습니다.");
        }

        AuthToken token = AuthToken.builder()
                .userId(user.getUserId())
                .tokenType(AuthToken.TYPE_PASSWORD_RESET)
                .tokenValue(UUID.randomUUID().toString())
                .build();

        authTokenMapper.insertToken(token, RESET_TOKEN_VALID_MINUTES);
        emailVerificationService.consume(Purpose.PASSWORD_RESET, email);

        return new ResetTokenResponse(token.getTokenValue());
    }

    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        String tokenValue = InputValidator.trimToNull(request == null ? null : request.resetToken());
        String password = InputValidator.requirePassword(request == null ? null : request.newPassword());

        AuthToken token = tokenValue == null ? null
                : authTokenMapper.selectUsableToken(tokenValue, AuthToken.TYPE_PASSWORD_RESET);

        if (token == null || authTokenMapper.markUsed(token.getTokenId()) != 1) {
            throw ApiException.badRequest("RESET_TOKEN_INVALID", "인증 시간이 지났습니다. 처음부터 다시 진행해 주세요.");
        }

        String salt = PasswordUtil.newSalt();
        userMapper.updatePasswordByUserId(token.getUserId(), PasswordUtil.hash(password, salt), salt);

        log.info("비밀번호 재설정 완료: userId={}", token.getUserId());
    }
}
