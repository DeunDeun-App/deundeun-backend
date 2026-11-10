package kopo.deundeunbackend.dto.response;

import kopo.deundeunbackend.domain.Guardian;

public record GuardianResponse(String relation, String name, String phone) {

    public static GuardianResponse from(Guardian guardian) {
        return guardian == null ? null
                : new GuardianResponse(guardian.getGuardianRelation(), guardian.getGuardianName(),
                        guardian.getGuardianPhone());
    }
}
