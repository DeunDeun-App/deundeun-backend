package kopo.deundeunbackend.controller;

import kopo.deundeunbackend.common.response.ApiResponse;
import kopo.deundeunbackend.dto.request.AuthRequest;
import kopo.deundeunbackend.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/email-code")
    public ApiResponse<Void> sendCode(@RequestBody AuthRequest request) {

        passwordResetService.sendCode(request);

        return ApiResponse.ok("인증번호를 보냈습니다. 메일함을 확인해 주세요.", null);
    }

    @PostMapping("/email-verify")
    public ApiResponse<Void> verifyCode(@RequestBody AuthRequest request) {

        passwordResetService.verifyCode(request);

        return ApiResponse.ok("인증이 완료되었습니다.", null);
    }

    @PostMapping("/reset-token")
    public ApiResponse<String> issueResetToken(@RequestBody AuthRequest request) {

        String resetToken = passwordResetService.issueResetToken(request);

        return ApiResponse.ok("본인 확인이 완료되었습니다.", resetToken);
    }

    @PostMapping("/reset")
    public ApiResponse<Void> resetPassword(@RequestBody AuthRequest request) {

        passwordResetService.resetPassword(request);

        return ApiResponse.ok("비밀번호가 변경되었습니다.", null);
    }
}
