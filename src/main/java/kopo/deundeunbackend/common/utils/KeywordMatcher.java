package kopo.deundeunbackend.common.utils;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

// 음성 인식 결과에 통과 키워드가 들어 있는지 판정
public final class KeywordMatcher {

    // 음성 인식은 띄어쓰기와 문장부호가 들쭉날쭉해서 비교 전에 모두 지운다
    private static final Pattern IGNORED = Pattern.compile("[\\s.,!?~\"'“”‘’·…]");

    private KeywordMatcher() {
    }

    public static boolean matches(String answer, List<String> keywords) {
        if (answer == null || keywords == null) {
            return false;
        }

        String normalized = normalize(answer);

        return keywords.stream()
                .map(KeywordMatcher::normalize)
                .anyMatch(keyword -> !keyword.isEmpty() && normalized.contains(keyword));
    }

    private static String normalize(String value) {
        return IGNORED.matcher(value).replaceAll("").toLowerCase(Locale.ROOT);
    }
}
