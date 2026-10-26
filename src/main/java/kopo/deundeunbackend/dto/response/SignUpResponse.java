package kopo.deundeunbackend.dto.response;

import kopo.deundeunbackend.domain.User;

public record SignUpResponse(Long userId, String email, String userName) {

    public static SignUpResponse from(User user) {
        return new SignUpResponse(user.getUserId(), user.getEmail(), user.getUserName());
    }
}
