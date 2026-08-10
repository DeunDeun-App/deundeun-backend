package kopo.deundeunbackend.service;

import kopo.deundeunbackend.common.exception.ApiException;
import kopo.deundeunbackend.common.utils.PasswordUtil;
import kopo.deundeunbackend.domain.User;
import kopo.deundeunbackend.dto.request.LoginRequest;
import kopo.deundeunbackend.dto.response.LoginResponse;
import kopo.deundeunbackend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 인증 관련 업무 로직. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;

    /**
     * 이메일과 비밀번호로 로그인
     *
     * @param request 로그인 요청
     * @return 로그인한 회원 정보
     * @throws ApiException 입력값이 비었거나 인증에 실패한 경우
     */
    public LoginResponse login(LoginRequest request) {

        String email = request == null ? null : trimToNull(request.email());
        String password = request == null ? null : request.password();

        if (email == null) {
            throw ApiException.badRequest("EMAIL_REQUIRED", "이메일을 입력해 주세요.");
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

        return LoginResponse.from(user);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }
}
