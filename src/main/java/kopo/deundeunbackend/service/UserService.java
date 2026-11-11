package kopo.deundeunbackend.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import kopo.deundeunbackend.common.exception.ApiException;
import kopo.deundeunbackend.common.utils.InputValidator;
import kopo.deundeunbackend.domain.Guardian;
import kopo.deundeunbackend.domain.User;
import kopo.deundeunbackend.dto.request.UserRequest;
import kopo.deundeunbackend.dto.response.NotificationResponse;
import kopo.deundeunbackend.dto.response.ProfileResponse;
import kopo.deundeunbackend.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final int NOTICE_LIMIT = 5;
    private static final int GUARDIAN_NAME_MAX_LENGTH = 50;
    private static final Set<String> DISABILITY_TYPES = Set.of("INTELLECTUAL", "AUTISM", "DEVELOPMENTAL");
    private static final Set<String> SEVERITIES = Set.of("SEVERE", "MILD", "UNKNOWN");
    private static final Set<String> RELATIONS = Set.of("MOTHER", "FATHER", "ETC");

    private final UserMapper userMapper;

    public ProfileResponse getProfile(Long userId) {
        return ProfileResponse.of(requireUser(userId), userMapper.selectPrimaryGuardian(userId));
    }

    @Transactional
    public ProfileResponse updateProfile(Long userId, UserRequest.Profile request) {
        User user = requireUser(userId);

        user.setUserName(InputValidator.requireName(request.name()));
        user.setUserPhone(InputValidator.optionalPhone(request.phone(), "PHONE_INVALID", "전화번호 형식이 올바르지 않습니다."));
        user.setBirthDate(parseBirthDate(request.birthDate()));
        user.setDisabilityType(pick(request.disabilityType(), DISABILITY_TYPES, null));
        user.setSeverity(pick(request.severity(), SEVERITIES, "UNKNOWN"));
        userMapper.updateProfile(user);

        saveGuardian(userId, request);
        log.info("내 정보 수정: userId={}", userId);

        return getProfile(userId);
    }

    public ProfileResponse updateSettings(Long userId, UserRequest.Settings request) {
        User user = requireUser(userId);

        userMapper.updateSettings(userId,
                valueOr(request.shareLocation(), user.getIsShareLocation()),
                valueOr(request.shareResult(), user.getIsShareResult()),
                valueOr(request.sosAlert(), user.getIsSosAlert()));

        return getProfile(userId);
    }

    public List<NotificationResponse> getRecentNotifications(Long userId) {
        return userMapper.selectRecentNotifications(userId, NOTICE_LIMIT).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public void withdraw(Long userId) {
        requireUser(userId);
        userMapper.deleteUser(userId);

        log.info("회원탈퇴: userId={}", userId);
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectByUserId(userId);

        if (user == null) {
            throw SessionService.sessionExpired();
        }
        return user;
    }

    private void saveGuardian(Long userId, UserRequest.Profile request) {
        String name = InputValidator.trimToNull(request.guardianName());
        String phone = InputValidator.trimToNull(request.guardianPhone());
        Guardian current = userMapper.selectPrimaryGuardian(userId);

        if (name == null && phone == null) {
            if (current != null) {
                userMapper.deleteGuardiansByUserId(userId);
            }
            return;
        }
        if (name == null) {
            throw ApiException.badRequest("GUARDIAN_NAME_REQUIRED", "보호자 이름을 입력해 주세요.");
        }
        if (phone == null) {
            throw ApiException.badRequest("GUARDIAN_PHONE_REQUIRED", "보호자 연락처를 입력해 주세요.");
        }
        if (name.length() > GUARDIAN_NAME_MAX_LENGTH) {
            throw ApiException.badRequest("GUARDIAN_NAME_TOO_LONG", "보호자 이름은 50자 이하로 입력해 주세요.");
        }

        Guardian guardian = Guardian.builder()
                .guardianId(current == null ? null : current.getGuardianId())
                .userId(userId)
                .guardianName(name)
                .guardianPhone(InputValidator.optionalPhone(phone, "GUARDIAN_PHONE_INVALID",
                        "보호자 연락처 형식이 올바르지 않습니다."))
                .guardianRelation(pick(request.guardianRelation(), RELATIONS, "ETC"))
                .build();

        if (current == null) {
            userMapper.insertGuardian(guardian);
        } else {
            userMapper.updateGuardian(guardian);
        }
    }

    private LocalDate parseBirthDate(String value) {
        String trimmed = InputValidator.trimToNull(value);
        if (trimmed == null) {
            return null;
        }

        try {
            LocalDate birthDate = LocalDate.parse(trimmed);
            if (!birthDate.isAfter(LocalDate.now())) {
                return birthDate;
            }
        } catch (DateTimeParseException ignored) {
        }
        throw ApiException.badRequest("BIRTH_DATE_INVALID", "생년월일이 올바르지 않습니다.");
    }

    private boolean valueOr(Boolean value, Boolean fallback) {
        return value != null ? value : Boolean.TRUE.equals(fallback);
    }

    private String pick(String value, Set<String> allowed, String fallback) {
        String trimmed = InputValidator.trimToNull(value);
        return trimmed != null && allowed.contains(trimmed) ? trimmed : fallback;
    }
}
