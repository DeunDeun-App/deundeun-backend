package kopo.deundeunbackend.common.utils;

import java.util.regex.Pattern;
import kopo.deundeunbackend.common.exception.ApiException;

public final class InputValidator {

    private static final int NAME_MAX_LENGTH = 50;
    private static final int EMAIL_MAX_LENGTH = 100;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 16;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern CODE_PATTERN = Pattern.compile("^\\d{6}$");
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:'\",.<>/?`~\\";

    private InputValidator() {
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String requireName(String value) {
        String name = trimToNull(value);

        if (name == null) {
            throw ApiException.badRequest("NAME_REQUIRED", "이름을 입력해 주세요.");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw ApiException.badRequest("NAME_TOO_LONG", "이름은 50자 이하로 입력해 주세요.");
        }
        return name;
    }

    public static String requireEmail(String value) {
        String email = trimToNull(value);

        if (email == null) {
            throw ApiException.badRequest("EMAIL_REQUIRED", "이메일 주소를 입력해 주세요.");
        }
        if (email.length() > EMAIL_MAX_LENGTH || !EMAIL_PATTERN.matcher(email).matches()) {
            throw ApiException.badRequest("EMAIL_INVALID", "이메일 주소 형식이 올바르지 않습니다.");
        }
        return email;
    }

    public static String requireCode(String value) {
        String code = trimToNull(value);

        if (code == null) {
            throw ApiException.badRequest("CODE_REQUIRED", "인증번호를 입력해 주세요.");
        }
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw ApiException.badRequest("CODE_MISMATCH", "인증번호가 일치하지 않습니다.");
        }
        return code;
    }

    public static String requirePassword(String password) {
        if (password == null || password.length() < PASSWORD_MIN_LENGTH
                || password.length() > PASSWORD_MAX_LENGTH) {
            throw ApiException.badRequest("PASSWORD_LENGTH", "비밀번호는 8~16자로 입력해 주세요.");
        }
        if (password.chars().noneMatch(c -> (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
            throw ApiException.badRequest("PASSWORD_NO_LETTER", "영문이 포함되지 않았습니다.");
        }
        if (password.chars().noneMatch(Character::isDigit)) {
            throw ApiException.badRequest("PASSWORD_NO_DIGIT", "숫자가 포함되지 않았습니다.");
        }
        if (password.chars().noneMatch(c -> SPECIAL_CHARS.indexOf(c) >= 0)) {
            throw ApiException.badRequest("PASSWORD_NO_SPECIAL", "특수문자가 포함되지 않았습니다.");
        }
        return password;
    }
}
