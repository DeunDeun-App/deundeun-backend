package kopo.deundeunbackend.controller;

import java.util.List;
import kopo.deundeunbackend.common.response.ApiResponse;
import kopo.deundeunbackend.config.LoginInterceptor;
import kopo.deundeunbackend.dto.request.ProfileUpdateRequest;
import kopo.deundeunbackend.dto.request.SettingsRequest;
import kopo.deundeunbackend.dto.response.NotificationResponse;
import kopo.deundeunbackend.dto.response.ProfileResponse;
import kopo.deundeunbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<ProfileResponse> getProfile(@RequestAttribute(LoginInterceptor.USER_ID) Long userId) {

        ProfileResponse response = userService.getProfile(userId);

        return ApiResponse.ok("내 정보를 불러왔습니다.", response);
    }

    @PutMapping
    public ApiResponse<ProfileResponse> updateProfile(@RequestAttribute(LoginInterceptor.USER_ID) Long userId,
                                                      @RequestBody ProfileUpdateRequest request) {

        ProfileResponse response = userService.updateProfile(userId, request);

        return ApiResponse.ok("저장되었습니다.", response);
    }

    @PatchMapping("/settings")
    public ApiResponse<ProfileResponse> updateSettings(@RequestAttribute(LoginInterceptor.USER_ID) Long userId,
                                                       @RequestBody SettingsRequest request) {

        ProfileResponse response = userService.updateSettings(userId, request);

        return ApiResponse.ok("설정이 변경되었습니다.", response);
    }

    @GetMapping("/notifications")
    public ApiResponse<List<NotificationResponse>> getNotifications(
            @RequestAttribute(LoginInterceptor.USER_ID) Long userId) {

        List<NotificationResponse> response = userService.getRecentNotifications(userId);

        return ApiResponse.ok("보호자 알림 내역을 불러왔습니다.", response);
    }

    @DeleteMapping
    public ApiResponse<Void> withdraw(@RequestAttribute(LoginInterceptor.USER_ID) Long userId) {

        userService.withdraw(userId);

        return ApiResponse.ok("회원탈퇴가 완료되었습니다.", null);
    }
}
