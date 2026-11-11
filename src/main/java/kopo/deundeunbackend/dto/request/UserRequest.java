package kopo.deundeunbackend.dto.request;

public final class UserRequest {

    private UserRequest() {
    }

    public record Profile(String name, String phone, String birthDate, String disabilityType, String severity,
                          String guardianRelation, String guardianName, String guardianPhone) {

    }

    public record Settings(Boolean shareLocation, Boolean shareResult, Boolean sosAlert) {

    }
}
