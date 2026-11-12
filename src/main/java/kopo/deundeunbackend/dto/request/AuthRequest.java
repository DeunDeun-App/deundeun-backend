package kopo.deundeunbackend.dto.request;

public record AuthRequest(String loginId, String name, String email, String password, String code,
                          String resetToken) {

}
