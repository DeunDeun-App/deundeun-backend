package kopo.deundeunbackend.dto.response;

import java.util.Map;
import kopo.deundeunbackend.domain.Guardian;

/**
 * SOS 를 받을 보호자
 *
 * @param relationLabel 화면에 보여줄 관계 이름 (예: 부모님)
 */
public record GuardianResponse(Long guardianId, String guardianName, String guardianPhone,
                               String relation, String relationLabel) {

    private static final Map<String, String> RELATION_LABELS = Map.of(
            "MOTHER", "엄마",
            "FATHER", "아빠",
            "PARENT", "부모님",
            "SIBLING", "형제자매",
            "SPOUSE", "배우자",
            "TEACHER", "선생님",
            "SOCIAL_WORKER", "사회복지사",
            "ETC", "보호자");

    public static GuardianResponse from(Guardian guardian) {
        String relation = guardian.getGuardianRelation();
        return new GuardianResponse(guardian.getGuardianId(), guardian.getGuardianName(),
                guardian.getGuardianPhone(), relation,
                RELATION_LABELS.getOrDefault(relation, "보호자"));
    }
}
