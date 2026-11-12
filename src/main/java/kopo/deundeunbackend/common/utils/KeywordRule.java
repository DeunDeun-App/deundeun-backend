package kopo.deundeunbackend.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 질문 하나의 통과 규칙
 *
 * 규칙은 단서 묶음(part) 여러 개로 이뤄진다. 묶음마다 키워드가 하나라도 나오면 그 단서를 말한 것으로 보고,
 * 모든 묶음을 말하면 통과다. 앞서 한 답변도 함께 본다. ("땅이 흔들려요" 다음 "건물이 무너졌어요" → 통과)
 *
 * 키워드 JSON 은 두 가지로 적을 수 있다.
 * <pre>
 * ["불", "화재"]                                         묶음 하나. 하나라도 말하면 통과
 * {"parts": [{"words": ["흔들"], "ask": "땅은 어떤가요?"},
 *            {"words": ["무너"], "ask": "주변 건물은 어떤가요?"}]}   모든 묶음을 말해야 통과
 * </pre>
 *
 * 묶음에 {@code "named": true} 를 주면 키워드 앞에 이름이 붙어 있어야 말한 것으로 본다.
 * 주소에서 "학교예요" 는 안 되고 "새솔초등학교예요", "새솔 초등학교", "역삼동" 은 되게 할 때 쓴다.
 *
 * @param parts 단서 묶음
 */
public record KeywordRule(List<Part> parts) {

    // 이름이 아닌 말. 장소 낱말 앞에 이것만 있으면 이름을 말하지 않은 것으로 본다
    private static final Set<String> NOT_NAMES = Set.of(
            "우리", "저희", "제", "내", "나", "그", "저", "이", "요", "여기", "거기", "저기",
            "집", "근처", "큰", "작은", "동네", "어느", "무슨", "어떤", "몰라", "몰라요", "모르겠어요", "그냥");

    private static final Pattern IGNORED = Pattern.compile("[.,!?~\"'“”‘’·…]");

    /**
     * 단서 묶음 하나
     *
     * @param words 이 단서를 말했다고 볼 키워드
     * @param ask   이 단서를 빠뜨렸을 때 상담원이 되묻는 말. 없으면 {@code null}
     * @param named 키워드 앞에 이름이 붙어 있어야 하는지
     */
    public record Part(List<String> words, String ask, Boolean named) {

        boolean needsName() {
            return Boolean.TRUE.equals(named);
        }
    }

    /**
     * 판정 결과
     *
     * @param matched 지금까지 말한 단서 수
     * @param total   단서 수
     * @param missing 아직 말하지 않은 단서
     */
    public record Judgement(int matched, int total, List<Part> missing) {

        public boolean passed() {
            return total > 0 && matched == total;
        }
    }

    public static KeywordRule anyOf(List<String> words) {
        return new KeywordRule(List.of(new Part(words, null, false)));
    }

    /** 답변들을 모두 합쳐 몇 개의 단서를 말했는지 본다. */
    public Judgement judge(List<String> answers) {
        int matched = 0;
        List<Part> missing = new ArrayList<>();

        for (Part part : parts) {
            boolean said = answers.stream().anyMatch(answer -> part.needsName()
                    ? matchesWithName(answer, part.words())
                    : KeywordMatcher.matches(answer, part.words()));
            if (said) {
                matched++;
            } else {
                missing.add(part);
            }
        }

        return new Judgement(matched, parts.size(), missing);
    }

    /**
     * 키워드 앞에 이름이 붙어 있는지
     *
     * 같은 낱말 안에서 앞에 글자가 있거나(역삼동, 새솔초등학교), 띄어 쓴 바로 앞 낱말이 이름이면(새솔 초등학교) 된다.
     * 앞에 붙은 것이 다른 키워드거나 이름이 아닌 말(우리, 그, 집…)이면 안 된다. (초등학교, 우리 학교, 학교 앞)
     */
    static boolean matchesWithName(String answer, List<String> words) {
        if (answer == null || words == null) {
            return false;
        }
        String[] tokens = IGNORED.matcher(answer).replaceAll(" ").trim().split("\\s+");

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            for (String word : words) {
                if (word == null || word.isEmpty()) {
                    continue;
                }
                int at = token.indexOf(word);
                if (at < 0) {
                    continue;
                }
                String before = at > 0 ? token.substring(0, at) : (i > 0 ? tokens[i - 1] : "");
                if (isName(before, words)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isName(String before, List<String> words) {
        // "초등학교 앞" 의 "초등학교" 처럼 앞에 붙은 것이 장소 낱말뿐이면 이름이 아니다.
        // 끝에 붙은 장소 낱말을 다 떼어 내고 남는 것이 있어야 이름이다 (새솔초등학교 → 새솔)
        String rest = before;
        boolean stripped = true;
        while (stripped && !rest.isEmpty()) {
            stripped = false;
            for (String word : words) {
                if (word != null && !word.isEmpty() && rest.endsWith(word)) {
                    rest = rest.substring(0, rest.length() - word.length());
                    stripped = true;
                    break;
                }
            }
        }
        return !rest.isEmpty() && !NOT_NAMES.contains(rest);
    }
}
