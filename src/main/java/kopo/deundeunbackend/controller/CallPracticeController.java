package kopo.deundeunbackend.controller;

import kopo.deundeunbackend.common.response.ApiResponse;
import kopo.deundeunbackend.dto.request.CallRequest;
import kopo.deundeunbackend.dto.response.CallAnswerResponse;
import kopo.deundeunbackend.dto.response.CallResultResponse;
import kopo.deundeunbackend.dto.response.CallSessionResponse;
import kopo.deundeunbackend.service.CallPracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 119 신고 연습 API. 회원은 로그인 토큰으로 확인한다. */
@RestController
@RequestMapping("/call/sessions")
@RequiredArgsConstructor
public class CallPracticeController {

    private final CallPracticeService callPracticeService;

    @PostMapping
    public ApiResponse<CallSessionResponse> startSession(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {

        CallSessionResponse response = callPracticeService.startSession(authorization);

        return ApiResponse.ok("신고 연습을 시작합니다.", response);
    }

    @PostMapping("/{sessionId}/answers")
    public ApiResponse<CallAnswerResponse> answer(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long sessionId,
            @RequestBody CallRequest request) {

        CallAnswerResponse response = callPracticeService.answer(authorization, sessionId, request);

        return ApiResponse.ok(response.passed() ? "잘 말했어요." : "조금 더 자세히 말해 주세요.", response);
    }

    @PostMapping("/{sessionId}/complete")
    public ApiResponse<CallResultResponse> complete(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @PathVariable Long sessionId) {

        CallResultResponse response = callPracticeService.complete(authorization, sessionId);

        return ApiResponse.ok("연습을 마쳤어요.", response);
    }
}
