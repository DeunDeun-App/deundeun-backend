package kopo.deundeunbackend.controller;

import kopo.deundeunbackend.common.response.ApiResponse;
import kopo.deundeunbackend.dto.request.LoginRequest;
import kopo.deundeunbackend.dto.response.LoginResponse;
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
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ApiResponse.ok("로그인 되었습니다.", response);
    }
}
