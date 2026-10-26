package kopo.deundeunbackend.dto.request;

public record AuthRequest(String name, String email, String password, String code, String resetToken) {

}
