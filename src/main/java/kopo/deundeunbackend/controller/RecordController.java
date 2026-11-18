package kopo.deundeunbackend.controller;

import kopo.deundeunbackend.common.response.ApiResponse;
import kopo.deundeunbackend.dto.response.RecordResponse;
import kopo.deundeunbackend.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 기록 화면 API. 회원은 로그인 토큰으로 확인한다. */
@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    @GetMapping
    public ApiResponse<RecordResponse> getRecords(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {

        return ApiResponse.ok("기록을 불러왔어요.", recordService.getRecords(authorization));
    }
}
