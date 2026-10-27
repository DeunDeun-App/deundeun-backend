package kopo.deundeunbackend.controller;

import kopo.deundeunbackend.common.response.ApiResponse;
import kopo.deundeunbackend.dto.request.AuthRequest;
import kopo.deundeunbackend.dto.response.UserResponse;
import kopo.deundeunbackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증 API. */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인
     *
     * @param request 이메일과 비밀번호
     * @return 성공 시 회원 정보
     */
    @PostMapping("/login")
    public ApiResponse<UserResponse> login(@RequestBody AuthRequest request) {

        UserResponse response = authService.login(request);

        return ApiResponse.ok("로그인되었습니다.", response);
    }

    @PostMapping("/signup/email-code")
    public ApiResponse<Void> sendSignUpCode(@RequestBody AuthRequest request) {

        authService.sendSignUpCode(request);

        return ApiResponse.ok("인증번호를 보냈습니다. 메일함을 확인해 주세요.", null);
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

    @PostMapping("/find-email")
    public ApiResponse<Void> findEmail(@RequestBody AuthRequest request) {

        authService.findEmail(request);

        return ApiResponse.ok("일치하는 이메일이 확인되었습니다.", null);
    }
}
