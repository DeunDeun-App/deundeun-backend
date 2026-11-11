package kopo.deundeunbackend.service;

import java.util.Locale;
import java.util.UUID;
import kopo.deundeunbackend.common.exception.ApiException;
import kopo.deundeunbackend.common.utils.InputValidator;
import kopo.deundeunbackend.common.utils.PasswordUtil;
import kopo.deundeunbackend.domain.AuthToken;
import kopo.deundeunbackend.domain.User;
import kopo.deundeunbackend.dto.request.AuthRequest;
import kopo.deundeunbackend.dto.response.LoginResponse;
import kopo.deundeunbackend.dto.response.UserResponse;
import kopo.deundeunbackend.mapper.AuthTokenMapper;
import kopo.deundeunbackend.mapper.UserMapper;
import kopo.deundeunbackend.service.EmailVerificationService.Purpose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 인증 관련 업무 로직. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int RESET_TOKEN_VALID_MINUTES = 10;

    private final UserMapper userMapper;

    private final AuthTokenMapper authTokenMapper;

    private final EmailVerificationService emailVerificationService;

    private final SessionService sessionService;

    public LoginResponse login(AuthRequest request) {
        String loginId = InputValidator.trimToNull(request.loginId());
        String password = request.password();

        if (loginId == null) {
            throw ApiException.badRequest("LOGIN_ID_REQUIRED", "아이디를 입력해 주세요.");
        }
        if (password == null || password.isEmpty()) {
            throw ApiException.badRequest("PASSWORD_REQUIRED", "비밀번호를 입력해 주세요.");
        }

        User user = userMapper.selectByLoginId(loginId.toLowerCase(Locale.ROOT));

        if (user == null || !PasswordUtil.matches(password, user.getPwSalt(), user.getPwHash())) {
            log.info("로그인 실패: loginId={}", loginId);
            throw ApiException.unauthorized("LOGIN_FAILED", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        log.info("로그인 성공: userId={}", user.getUserId());

        return new LoginResponse(sessionService.issue(user.getUserId()), UserResponse.from(user));
    }

    public UserResponse me(String authorization) {
        User user = userMapper.selectByUserId(sessionService.requireUserId(authorization));

        if (user == null) {
            throw SessionService.sessionExpired();
        }
        return UserResponse.from(user);
    }

    public void logout(String authorization) {
        sessionService.expire(authorization);
    }

    public long sendSignUpCode(AuthRequest request) {
        String email = InputValidator.requireEmail(request.email());

        if (userMapper.selectByEmail(email) != null) {
            throw emailDuplicated();
        }
        return emailVerificationService.sendCode(Purpose.SIGNUP, email);
    }

    public void verifySignUpCode(AuthRequest request) {
        verifyCode(Purpose.SIGNUP, request);
    }

    public UserResponse signUp(AuthRequest request) {
        String name = InputValidator.requireName(request.name());
        String loginId = InputValidator.requireLoginId(request.loginId());
        String email = InputValidator.requireEmail(request.email());
        String password = InputValidator.requirePassword(request.password());

        if (userMapper.selectByLoginId(loginId) != null) {
            throw loginIdDuplicated();
        }

        emailVerificationService.requireVerified(Purpose.SIGNUP, email);

        if (userMapper.selectByEmail(email) != null) {
            throw emailDuplicated();
        }

        String salt = PasswordUtil.newSalt();
        User user = User.builder()
                .loginId(loginId)
                .email(email)
                .pwSalt(salt)
                .pwHash(PasswordUtil.hash(password, salt))
                .userName(name)
                .build();

        try {
            userMapper.insertUser(user);

        } catch (DuplicateKeyException e) {
            throw loginIdDuplicated();
        }

        emailVerificationService.consume(Purpose.SIGNUP, email);
        log.info("회원가입 완료: userId={}", user.getUserId());

        return UserResponse.from(user);
    }

    public long sendFindLoginIdCode(AuthRequest request) {
        String name = InputValidator.requireName(request.name());
        String email = InputValidator.requireEmail(request.email());

        requireUserByNameAndEmail(name, email);

        return emailVerificationService.sendCode(Purpose.FIND_LOGIN_ID, email);
    }

    public String verifyFindLoginIdCode(AuthRequest request) {
        String name = InputValidator.requireName(request.name());
        String email = verifyCode(Purpose.FIND_LOGIN_ID, request);

        User user = requireUserByNameAndEmail(name, email);
        emailVerificationService.consume(Purpose.FIND_LOGIN_ID, email);

        return user.getLoginId();
    }

    public long sendPasswordResetCode(AuthRequest request) {
        String loginId = InputValidator.requireLoginId(request.loginId());
        String email = InputValidator.requireEmail(request.email());

        requireUserByLoginIdAndEmail(loginId, email);

        return emailVerificationService.sendCode(Purpose.PASSWORD_RESET, email);
    }

    public void verifyPasswordResetCode(AuthRequest request) {
        verifyCode(Purpose.PASSWORD_RESET, request);
    }

    public String issueResetToken(AuthRequest request) {
        String loginId = InputValidator.requireLoginId(request.loginId());
        String email = InputValidator.requireEmail(request.email());

        emailVerificationService.requireVerified(Purpose.PASSWORD_RESET, email);
        User user = requireUserByLoginIdAndEmail(loginId, email);

        AuthToken token = AuthToken.builder()
                .userId(user.getUserId())
                .tokenType(AuthToken.TYPE_PASSWORD_RESET)
                .tokenValue(UUID.randomUUID().toString())
                .build();

        authTokenMapper.insertToken(token, RESET_TOKEN_VALID_MINUTES);
        emailVerificationService.consume(Purpose.PASSWORD_RESET, email);

        return token.getTokenValue();
    }

    @Transactional
    public void resetPassword(AuthRequest request) {
        String tokenValue = InputValidator.trimToNull(request.resetToken());
        String password = InputValidator.requirePassword(request.password());

        AuthToken token = tokenValue == null ? null
                : authTokenMapper.selectUsableToken(tokenValue, AuthToken.TYPE_PASSWORD_RESET);

        if (token == null || authTokenMapper.markUsed(token.getTokenId()) != 1) {
            throw ApiException.badRequest("RESET_TOKEN_INVALID", "인증 시간이 지났습니다. 처음부터 다시 진행해 주세요.");
        }

        String salt = PasswordUtil.newSalt();
        userMapper.updatePasswordByUserId(token.getUserId(), PasswordUtil.hash(password, salt), salt);

        log.info("비밀번호 재설정 완료: userId={}", token.getUserId());
    }

    private String verifyCode(Purpose purpose, AuthRequest request) {
        String email = InputValidator.requireEmail(request.email());
        String code = InputValidator.requireCode(request.code());

        emailVerificationService.verify(purpose, email, code);
        return email;
    }

    private User requireUserByNameAndEmail(String name, String email) {
        User user = userMapper.selectByEmail(email);

        if (user == null) {
            throw ApiException.notFound("EMAIL_NOT_FOUND", "가입되지 않은 이메일입니다.");
        }
        if (!name.equals(user.getUserName())) {
            throw ApiException.badRequest("NAME_MISMATCH", "이름이 일치하지 않습니다.");
        }
        return user;
    }

    private User requireUserByLoginIdAndEmail(String loginId, String email) {
        User user = userMapper.selectByLoginId(loginId);

        if (user == null) {
            throw ApiException.notFound("LOGIN_ID_NOT_FOUND", "가입되지 않은 아이디입니다.");
        }
        if (!email.equalsIgnoreCase(user.getEmail())) {
            throw ApiException.badRequest("EMAIL_MISMATCH", "가입 시 등록한 이메일 주소와 일치하지 않습니다.");
        }
        return user;
    }

    private ApiException emailDuplicated() {
        return ApiException.conflict("EMAIL_DUPLICATED", "이미 가입된 이메일입니다.");
    }

    private ApiException loginIdDuplicated() {
        return ApiException.conflict("LOGIN_ID_DUPLICATED", "이미 사용 중인 아이디입니다.");
    }
}
