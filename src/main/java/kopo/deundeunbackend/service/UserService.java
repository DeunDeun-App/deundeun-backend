package kopo.deundeunbackend.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import kopo.deundeunbackend.common.exception.ApiException;
import kopo.deundeunbackend.common.utils.InputValidator;
import kopo.deundeunbackend.domain.Guardian;
import kopo.deundeunbackend.domain.User;
import kopo.deundeunbackend.dto.request.ProfileUpdateRequest;
import kopo.deundeunbackend.dto.request.SettingsRequest;
import kopo.deundeunbackend.dto.response.NotificationResponse;
import kopo.deundeunbackend.dto.response.ProfileResponse;
import kopo.deundeunbackend.mapper.GuardianMapper;
import kopo.deundeunbackend.mapper.NotificationMapper;
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
    private static final Pattern PHONE_PATTERN = Pattern.compile("^01[016789]\\d{7,8}$");
    private static final Set<String> DISABILITY_TYPES = Set.of("INTELLECTUAL", "AUTISM", "DEVELOPMENTAL");
    private static final Set<String> SEVERITIES = Set.of("SEVERE", "MILD", "UNKNOWN");
    private static final Set<String> RELATIONS = Set.of("MOTHER", "FATHER", "ETC");

    private final UserMapper userMapper;

    private final GuardianMapper guardianMapper;

    private final NotificationMapper notificationMapper;

    public ProfileResponse getProfile(Long userId) {
        User user = requireUser(userId);

        return ProfileResponse.of(user, guardianMapper.selectPrimary(userId));
    }

    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        if (request == null) {
            throw ApiException.badRequest("INVALID_REQUEST", "입력한 정보가 올바르지 않습니다.");
        }

        User user = requireUser(userId);

        user.setUserName(InputValidator.requireName(request.name()));
        user.setUserPhone(normalizePhone(request.phone(), "PHONE_INVALID", "전화번호 형식이 올바르지 않습니다."));
        user.setBirthDate(parseBirthDate(request.birthDate()));
        user.setDisabilityType(pick(request.disabilityType(), DISABILITY_TYPES, null));
        user.setSeverity(pick(request.severity(), SEVERITIES, "UNKNOWN"));
        userMapper.updateProfile(user);

        saveGuardian(userId, request);

        log.info("내 정보 수정: userId={}", userId);

        return getProfile(userId);
    }

    public ProfileResponse updateSettings(Long userId, SettingsRequest request) {
        User user = requireUser(userId);

        boolean shareLocation = request == null || request.shareLocation() == null
                ? Boolean.TRUE.equals(user.getIsShareLocation()) : request.shareLocation();
        boolean shareResult = request == null || request.shareResult() == null
                ? Boolean.TRUE.equals(user.getIsShareResult()) : request.shareResult();
        boolean sosAlert = request == null || request.sosAlert() == null
                ? Boolean.TRUE.equals(user.getIsSosAlert()) : request.sosAlert();

        userMapper.updateSettings(userId, shareLocation, shareResult, sosAlert);

        return getProfile(userId);
    }

    public List<NotificationResponse> getRecentNotifications(Long userId) {
        return notificationMapper.selectRecent(userId, NOTICE_LIMIT).stream()
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
            throw ApiException.unauthorized("SESSION_EXPIRED", "로그인이 필요합니다. 다시 로그인해 주세요.");
        }
        return user;
    }

    private void saveGuardian(Long userId, ProfileUpdateRequest request) {
        String name = InputValidator.trimToNull(request.guardianName());
        String phone = InputValidator.trimToNull(request.guardianPhone());
        Guardian current = guardianMapper.selectPrimary(userId);

        if (name == null && phone == null) {
            if (current != null) {
                guardianMapper.deleteByUserId(userId);
            }
            return;
        }
        if (name == null) {
            throw ApiException.badRequest("GUARDIAN_NAME_REQUIRED", "보호자 이름을 입력해 주세요.");
        }
        if (phone == null) {
            throw ApiException.badRequest("GUARDIAN_PHONE_REQUIRED", "보호자 연락처를 입력해 주세요.");
        }
        if (name.length() > 50) {
            throw ApiException.badRequest("GUARDIAN_NAME_TOO_LONG", "보호자 이름은 50자 이하로 입력해 주세요.");
        }

        Guardian guardian = Guardian.builder()
                .guardianId(current == null ? null : current.getGuardianId())
                .userId(userId)
                .guardianName(name)
                .guardianPhone(normalizePhone(phone, "GUARDIAN_PHONE_INVALID", "보호자 연락처 형식이 올바르지 않습니다."))
                .guardianRelation(pick(request.guardianRelation(), RELATIONS, "ETC"))
                .build();

        if (current == null) {
            guardianMapper.insertGuardian(guardian);
        } else {
            guardianMapper.updateGuardian(guardian);
        }
    }

    private String normalizePhone(String value, String code, String message) {
        String trimmed = InputValidator.trimToNull(value);
        if (trimmed == null) {
            return null;
        }

        String digits = trimmed.replaceAll("[\\s-]", "");
        if (!PHONE_PATTERN.matcher(digits).matches()) {
            throw ApiException.badRequest(code, message);
        }

        int middleEnd = digits.length() == 11 ? 7 : 6;
        return digits.substring(0, 3) + "-" + digits.substring(3, middleEnd) + "-" + digits.substring(middleEnd);
    }

    private LocalDate parseBirthDate(String value) {
        String trimmed = InputValidator.trimToNull(value);
        if (trimmed == null) {
            return null;
        }

        try {
            LocalDate birthDate = LocalDate.parse(trimmed);
            if (birthDate.isAfter(LocalDate.now())) {
                throw ApiException.badRequest("BIRTH_DATE_INVALID", "생년월일이 올바르지 않습니다.");
            }
            return birthDate;

        } catch (DateTimeParseException e) {
            throw ApiException.badRequest("BIRTH_DATE_INVALID", "생년월일이 올바르지 않습니다.");
        }
    }

    private String pick(String value, Set<String> allowed, String fallback) {
        String trimmed = InputValidator.trimToNull(value);
        return trimmed != null && allowed.contains(trimmed) ? trimmed : fallback;
    }
}
