package kopo.deundeunbackend.service;

import kopo.deundeunbackend.common.exception.ApiException;
import kopo.deundeunbackend.common.utils.InputValidator;
import kopo.deundeunbackend.common.utils.PasswordUtil;
import kopo.deundeunbackend.domain.User;
import kopo.deundeunbackend.dto.request.AuthRequest;
import kopo.deundeunbackend.dto.response.UserResponse;
import kopo.deundeunbackend.mapper.UserMapper;
import kopo.deundeunbackend.service.EmailVerificationService.Purpose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/** 인증 관련 업무 로직. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;

    private final EmailVerificationService emailVerificationService;

    /**
     * 이메일과 비밀번호로 로그인
     *
     * @param request 로그인 요청
     * @return 로그인한 회원 정보
     * @throws ApiException 입력값이 비었거나 인증에 실패한 경우
     */
    public UserResponse login(AuthRequest request) {

        String email = request == null ? null : InputValidator.trimToNull(request.email());
        String password = request == null ? null : request.password();

        if (email == null) {
            throw ApiException.badRequest("EMAIL_REQUIRED", "이메일 주소를 입력해 주세요.");
        }
        if (password == null || password.isEmpty()) {
            throw ApiException.badRequest("PASSWORD_REQUIRED", "비밀번호를 입력해 주세요.");
        }

        User user = userMapper.selectByEmail(email);

        if (user == null || !PasswordUtil.matches(password, user.getPwSalt(), user.getPwHash())) {
            log.info("로그인 실패: email={}", email);
            throw ApiException.unauthorized("LOGIN_FAILED", "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        log.info("로그인 성공: userId={}", user.getUserId());

        return UserResponse.from(user);
    }

    public long sendSignUpCode(AuthRequest request) {
        String email = InputValidator.requireEmail(request == null ? null : request.email());

        if (userMapper.selectByEmail(email) != null) {
            throw ApiException.conflict("EMAIL_DUPLICATED", "이미 가입된 이메일입니다.");
        }

        return emailVerificationService.sendCode(Purpose.SIGNUP, email);
    }

    public void verifySignUpCode(AuthRequest request) {
        String email = InputValidator.requireEmail(request == null ? null : request.email());
        String code = InputValidator.requireCode(request.code());

        emailVerificationService.verify(Purpose.SIGNUP, email, code);
    }

    public UserResponse signUp(AuthRequest request) {
        if (request == null) {
            throw ApiException.badRequest("INVALID_REQUEST", "입력한 정보가 올바르지 않습니다.");
        }

        String name = InputValidator.requireName(request.name());
        String email = InputValidator.requireEmail(request.email());
        String password = InputValidator.requirePassword(request.password());

        emailVerificationService.requireVerified(Purpose.SIGNUP, email);

        if (userMapper.selectByEmail(email) != null) {
            throw ApiException.conflict("EMAIL_DUPLICATED", "이미 가입된 이메일입니다.");
        }

        String salt = PasswordUtil.newSalt();
        User user = User.builder()
                .email(email)
                .pwSalt(salt)
                .pwHash(PasswordUtil.hash(password, salt))
                .userName(name)
                .build();

        try {
            userMapper.insertUser(user);

        } catch (DuplicateKeyException e) {
            throw ApiException.conflict("EMAIL_DUPLICATED", "이미 가입된 이메일입니다.");
        }

        emailVerificationService.consume(Purpose.SIGNUP, email);
        log.info("회원가입 완료: userId={}", user.getUserId());

        return UserResponse.from(user);
    }

    public void findEmail(AuthRequest request) {
        String name = InputValidator.requireName(request == null ? null : request.name());
        String email = InputValidator.requireEmail(request.email());

        User user = userMapper.selectByEmail(email);

        if (user == null) {
            throw ApiException.notFound("EMAIL_NOT_FOUND", "가입되지 않은 이메일입니다.");
        }
        if (!name.equals(user.getUserName())) {
            throw ApiException.badRequest("NAME_MISMATCH", "이름이 일치하지 않습니다.");
        }
    }
}
