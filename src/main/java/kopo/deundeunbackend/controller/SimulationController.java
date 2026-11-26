package kopo.deundeunbackend.controller;

import kopo.deundeunbackend.common.response.ApiResponse;
import kopo.deundeunbackend.config.LoginInterceptor;
import kopo.deundeunbackend.dto.request.SimulationRequest;
import kopo.deundeunbackend.dto.response.SimulationResultResponse;
import kopo.deundeunbackend.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulation")
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationService simulationService;

    @PostMapping("/results")
    public ApiResponse<SimulationResultResponse> saveResult(@RequestAttribute(LoginInterceptor.USER_ID) Long userId,
                                                            @RequestBody SimulationRequest request) {
        return ApiResponse.ok("훈련을 마쳤어요.", simulationService.saveResult(userId, request));
    }
}
