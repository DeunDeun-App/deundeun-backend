package kopo.deundeunbackend.controller;

import kopo.deundeunbackend.common.response.ApiResponse;
import kopo.deundeunbackend.dto.request.SimulationRequest;
import kopo.deundeunbackend.dto.response.SimulationResultResponse;
import kopo.deundeunbackend.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 재난 시뮬레이션 결과 API. 회원은 로그인 토큰으로 확인한다. */
@RestController
@RequestMapping("/simulation")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationService simulationService;

    @PostMapping("/results")
    public ApiResponse<SimulationResultResponse> saveResult(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody SimulationRequest request) {

        SimulationResultResponse response = simulationService.saveResult(authorization, request);

        return ApiResponse.ok("훈련을 마쳤어요.", response);
    }
}
