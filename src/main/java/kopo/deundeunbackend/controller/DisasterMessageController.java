package kopo.deundeunbackend.controller;

import java.util.List;
import kopo.deundeunbackend.common.response.ApiResponse;
import kopo.deundeunbackend.dto.response.DisasterMessageResponse;
import kopo.deundeunbackend.dto.response.EasyMessageResponse;
import kopo.deundeunbackend.service.DisasterMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/disaster-messages")
@RequiredArgsConstructor
public class DisasterMessageController {

    private final DisasterMessageService disasterMessageService;

    @GetMapping
    public ApiResponse<List<DisasterMessageResponse>> getRecentMessages(
            @RequestParam(defaultValue = "false") boolean refresh) {

        List<DisasterMessageResponse> response = disasterMessageService.getRecentMessages(refresh);

        return ApiResponse.ok("최근 재난문자를 불러왔습니다.", response);
    }

    @PostMapping("/{id}/easy")
    public ApiResponse<EasyMessageResponse> toEasyMessage(@PathVariable String id) {

        EasyMessageResponse response = disasterMessageService.toEasyMessage(id);

        return ApiResponse.ok("쉬운 말로 바꿨습니다.", response);
    }
}
