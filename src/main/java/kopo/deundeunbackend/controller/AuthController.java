package kopo.deundeunbackend.controller;

import kopo.deundeunbackend.common.response.ApiResponse;
import kopo.deundeunbackend.dto.request.AuthRequest;
import kopo.deundeunbackend.dto.response.LoginResponse;
import kopo.deundeunbackend.dto.response.UserResponse;
import kopo.deundeunbackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증 API. */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인
     *
     * @param request 아이디와 비밀번호
     * @return 성공 시 로그인 유지용 토큰과 회원 정보
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody AuthRequest request) {

        LoginResponse response = authService.login(request);

        return ApiResponse.ok("로그인되었습니다.", response);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {

        UserResponse response = authService.me(authorization);

        return ApiResponse.ok("로그인 상태입니다.", response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {

        authService.logout(authorization);

        return ApiResponse.ok("로그아웃되었습니다.", null);
    }

    @PostMapping("/signup/email-code")
    public ApiResponse<Long> sendSignUpCode(@RequestBody AuthRequest request) {

        long validSeconds = authService.sendSignUpCode(request);

        return ApiResponse.ok("인증번호를 보냈습니다. 메일함을 확인해 주세요.", validSeconds);
    }

    @PostMapping("/signup/email-verify")
    public ApiResponse<Void> verifySignUpCode(@RequestBody AuthRequest request) {

        authService.verifySignUpCode(request);

        return ApiResponse.ok("인증이 완료되었습니다.", null);
    }

    @PostMapping("/signup")
    public ApiResponse<UserResponse> signUp(@RequestBody AuthRequest request) {

        UserResponse response = authService.signUp(request);

        return ApiResponse.ok("회원가입이 완료되었습니다.", response);
    }

    @PostMapping("/find-id/email-code")
    public ApiResponse<Long> sendFindLoginIdCode(@RequestBody AuthRequest request) {

        long validSeconds = authService.sendFindLoginIdCode(request);

        return ApiResponse.ok("인증번호를 보냈습니다. 메일함을 확인해 주세요.", validSeconds);
    }

    @PostMapping("/find-id/email-verify")
    public ApiResponse<String> verifyFindLoginIdCode(@RequestBody AuthRequest request) {

        String loginId = authService.verifyFindLoginIdCode(request);

        return ApiResponse.ok("아이디를 찾았습니다.", loginId);
    }
}
