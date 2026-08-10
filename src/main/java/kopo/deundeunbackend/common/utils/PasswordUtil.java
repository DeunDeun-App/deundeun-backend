package kopo.deundeunbackend.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

// 비밀번호 솔팅/해싱 유틸
public final class PasswordUtil {

    private static final int SALT_BYTE_LENGTH = 16;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();

    private PasswordUtil() {
    }

    public static String newSalt() {
        byte[] salt = new byte[SALT_BYTE_LENGTH];
        RANDOM.nextBytes(salt);

        return HEX.formatHex(salt);
    }

    // 평문 비밀번호를 저장 가능한 해시로 변환
    public static String hash(String rawPassword, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest((salt + rawPassword).getBytes(StandardCharsets.UTF_8));

            return HEX.formatHex(hashed);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    // 입력한 비밀번호가 저장된 해시와 일치하는지 확인
    public static boolean matches(String rawPassword, String salt, String expectedHash) {
        if (rawPassword == null || salt == null || expectedHash == null) {
            return false;
        }

        byte[] actual = hash(rawPassword, salt).getBytes(StandardCharsets.UTF_8);
        byte[] expected = expectedHash.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(actual, expected);
    }
}
