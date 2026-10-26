package kopo.deundeunbackend.dto.response;

import kopo.deundeunbackend.domain.User;

public record UserResponse(Long userId, String email, String userName) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getUserId(), user.getEmail(), user.getUserName());
    }
}
