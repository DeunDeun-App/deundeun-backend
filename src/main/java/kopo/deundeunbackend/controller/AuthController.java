package kopo.deundeunbackend.controller;

import kopo.deundeunbackend.common.response.ApiResponse;
import kopo.deundeunbackend.dto.request.AuthRequest;
import kopo.deundeunbackend.dto.response.LoginResponse;
import kopo.deundeunbackend.dto.response.UserResponse;
import kopo.deundeunbackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증 API. */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String CODE_SENT = "인증번호를 보냈습니다. 메일함을 확인해 주세요.";
    private static final String CODE_VERIFIED = "인증이 완료되었습니다.";

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody AuthRequest request) {
        return ApiResponse.ok("로그인되었습니다.", authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return ApiResponse.ok("로그인 상태입니다.", authService.me(authorization));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        authService.logout(authorization);
        return ApiResponse.ok("로그아웃되었습니다.", null);
    }

    @PostMapping("/signup/email-code")
    public ApiResponse<Long> sendSignUpCode(@RequestBody AuthRequest request) {
        return ApiResponse.ok(CODE_SENT, authService.sendSignUpCode(request));
    }

    @PostMapping("/signup/email-verify")
    public ApiResponse<Void> verifySignUpCode(@RequestBody AuthRequest request) {
        authService.verifySignUpCode(request);
        return ApiResponse.ok(CODE_VERIFIED, null);
    }

    @PostMapping("/signup")
    public ApiResponse<UserResponse> signUp(@RequestBody AuthRequest request) {
        return ApiResponse.ok("회원가입이 완료되었습니다.", authService.signUp(request));
    }

    @PostMapping("/find-id/email-code")
    public ApiResponse<Long> sendFindLoginIdCode(@RequestBody AuthRequest request) {
        return ApiResponse.ok(CODE_SENT, authService.sendFindLoginIdCode(request));
    }

    @PostMapping("/find-id/email-verify")
    public ApiResponse<String> verifyFindLoginIdCode(@RequestBody AuthRequest request) {
        return ApiResponse.ok("아이디를 찾았습니다.", authService.verifyFindLoginIdCode(request));
    }

    @PostMapping("/password/email-code")
    public ApiResponse<Long> sendPasswordResetCode(@RequestBody AuthRequest request) {
        return ApiResponse.ok(CODE_SENT, authService.sendPasswordResetCode(request));
    }

    @PostMapping("/password/email-verify")
    public ApiResponse<Void> verifyPasswordResetCode(@RequestBody AuthRequest request) {
        authService.verifyPasswordResetCode(request);
        return ApiResponse.ok(CODE_VERIFIED, null);
    }

    @PostMapping("/password/reset-token")
    public ApiResponse<String> issueResetToken(@RequestBody AuthRequest request) {
        return ApiResponse.ok("본인 확인이 완료되었습니다.", authService.issueResetToken(request));
    }

    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@RequestBody AuthRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok("비밀번호가 변경되었습니다.", null);
    }
}
