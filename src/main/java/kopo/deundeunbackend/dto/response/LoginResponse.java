package kopo.deundeunbackend.dto.response;

import kopo.deundeunbackend.domain.User;

public record LoginResponse(Long userId, String email, String userName) {

    public static LoginResponse from(User user) {
        return new LoginResponse(user.getUserId(), user.getEmail(), user.getUserName());
    }
}
