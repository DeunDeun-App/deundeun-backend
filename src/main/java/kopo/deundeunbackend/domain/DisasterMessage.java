package kopo.deundeunbackend.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public record DisasterMessage(String id, String region, String text, String step, String category,
                              String createdAt) {

    public static DisasterMessage of(String region, String text, String step, String category, String createdAt) {
        return new DisasterMessage(hash(text), region, text, step, category, createdAt);
    }

    private static String hash(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }
}
