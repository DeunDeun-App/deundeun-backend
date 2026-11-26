package kopo.deundeunbackend.controller;

import kopo.deundeunbackend.common.response.ApiResponse;
import kopo.deundeunbackend.config.LoginInterceptor;
import kopo.deundeunbackend.dto.response.RecordResponse;
import kopo.deundeunbackend.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @GetMapping
    public ApiResponse<RecordResponse> getRecords(@RequestAttribute(LoginInterceptor.USER_ID) Long userId) {
        return ApiResponse.ok("기록을 불러왔어요.", recordService.getRecords(userId));
    }
}
