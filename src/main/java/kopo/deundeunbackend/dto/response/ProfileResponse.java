package kopo.deundeunbackend.dto.response;

import kopo.deundeunbackend.domain.Guardian;
import kopo.deundeunbackend.domain.User;

public record ProfileResponse(String loginId, String email, String name, String phone, String birthDate,
                              String disabilityType, String severity, boolean shareLocation,
                              boolean shareResult, boolean sosAlert, GuardianInfo guardian) {

    public static ProfileResponse of(User user, Guardian guardian) {
        return new ProfileResponse(
                user.getLoginId(),
                user.getEmail(),
                user.getUserName(),
                user.getUserPhone(),
                user.getBirthDate() == null ? null : user.getBirthDate().toString(),
                user.getDisabilityType(),
                user.getSeverity(),
                Boolean.TRUE.equals(user.getIsShareLocation()),
                Boolean.TRUE.equals(user.getIsShareResult()),
                Boolean.TRUE.equals(user.getIsSosAlert()),
                guardian == null ? null
                        : new GuardianInfo(guardian.getGuardianRelation(), guardian.getGuardianName(),
                                guardian.getGuardianPhone()));
    }

    public record GuardianInfo(String relation, String name, String phone) {

    }
}
