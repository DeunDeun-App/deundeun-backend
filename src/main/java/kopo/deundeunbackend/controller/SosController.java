package kopo.deundeunbackend.controller;

import java.util.List;
import kopo.deundeunbackend.common.response.ApiResponse;
import kopo.deundeunbackend.config.LoginInterceptor;
import kopo.deundeunbackend.dto.request.SosRequest;
import kopo.deundeunbackend.dto.response.GuardianResponse;
import kopo.deundeunbackend.dto.response.SosResponse;
import kopo.deundeunbackend.service.SosService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sos")
@RequiredArgsConstructor
public class SosController {

    private final SosService sosService;

    @GetMapping("/guardians")
    public ApiResponse<List<GuardianResponse>> getGuardians(@RequestAttribute(LoginInterceptor.USER_ID) Long userId) {
        return ApiResponse.ok("보호자 정보를 불러왔어요.", sosService.getGuardians(userId));
    }

    @PostMapping
    public ApiResponse<SosResponse> request(@RequestAttribute(LoginInterceptor.USER_ID) Long userId,
                                            @RequestBody SosRequest request) {
        return ApiResponse.ok("보호자에게 보낼 메시지를 준비했어요.", sosService.request(userId, request));
    }

    @PostMapping("/{sosId}/sent")
    public ApiResponse<Void> markSent(@RequestAttribute(LoginInterceptor.USER_ID) Long userId,
                                      @PathVariable Long sosId) {
        sosService.markSent(userId, sosId);
        return ApiResponse.ok("보호자에게 도움을 요청했어요.", null);
    }
}
