package kopo.deundeunbackend.controller;

import kopo.deundeunbackend.common.response.ApiResponse;
import kopo.deundeunbackend.config.LoginInterceptor;
import kopo.deundeunbackend.dto.request.CallRequest;
import kopo.deundeunbackend.dto.response.CallAnswerResponse;
import kopo.deundeunbackend.dto.response.CallResultResponse;
import kopo.deundeunbackend.dto.response.CallSessionResponse;
import kopo.deundeunbackend.service.CallPracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/call/sessions")
@RequiredArgsConstructor
public class CallPracticeController {

    private final CallPracticeService callPracticeService;

    @PostMapping
    public ApiResponse<CallSessionResponse> startSession(@RequestAttribute(LoginInterceptor.USER_ID) Long userId) {
        return ApiResponse.ok("신고 연습을 시작합니다.", callPracticeService.startSession(userId));
    }

    @PostMapping("/{sessionId}/answers")
    public ApiResponse<CallAnswerResponse> answer(@RequestAttribute(LoginInterceptor.USER_ID) Long userId,
                                                  @PathVariable Long sessionId,
                                                  @RequestBody CallRequest request) {
        CallAnswerResponse response = callPracticeService.answer(userId, sessionId, request);
        return ApiResponse.ok(response.passed() ? "잘 말했어요." : "조금 더 자세히 말해 주세요.", response);
    }

    @PostMapping("/{sessionId}/complete")
    public ApiResponse<CallResultResponse> complete(@RequestAttribute(LoginInterceptor.USER_ID) Long userId,
                                                    @PathVariable Long sessionId) {
        return ApiResponse.ok("연습을 마쳤어요.", callPracticeService.complete(userId, sessionId));
    }
}
