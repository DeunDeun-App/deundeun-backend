package kopo.deundeunbackend.dto.request;

public record ProfileUpdateRequest(String name, String phone, String birthDate, String disabilityType,
                                   String severity, String guardianRelation, String guardianName,
                                   String guardianPhone) {

}
