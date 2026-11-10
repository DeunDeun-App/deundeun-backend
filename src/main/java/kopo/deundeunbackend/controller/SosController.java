package kopo.deundeunbackend.controller;

import java.util.List;
import kopo.deundeunbackend.common.response.ApiResponse;
import kopo.deundeunbackend.dto.request.SosRequest;
import kopo.deundeunbackend.dto.response.GuardianResponse;
import kopo.deundeunbackend.dto.response.SosResponse;
import kopo.deundeunbackend.service.SosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** SOS 도움 요청 API. 회원은 로그인 토큰으로 확인한다. */
@RestController
@RequestMapping("/sos")
@RequiredArgsConstructor
public class SosController {

    private final SosService sosService;

    @GetMapping("/guardians")
    public ApiResponse<List<GuardianResponse>> getGuardians(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {

        List<GuardianResponse> response = sosService.getGuardians(authorization);

        return ApiResponse.ok("보호자 정보를 불러왔어요.", response);
    }

    @PostMapping
    public ApiResponse<SosResponse> request(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody SosRequest request) {

        SosResponse response = sosService.request(authorization, request);

        return ApiResponse.ok("보호자에게 보낼 메시지를 준비했어요.", response);
    }

    @PostMapping("/{sosId}/sent")
    public ApiResponse<Void> markSent(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long sosId) {

        sosService.markSent(authorization, sosId);

        return ApiResponse.ok("보호자에게 도움을 요청했어요.", null);
    }
}
