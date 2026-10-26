package kopo.deundeunbackend.dto.request;

public record PasswordResetRequest(String resetToken, String newPassword) {

}
