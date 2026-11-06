package kopo.deundeunbackend.dto.response;

/**
 * 답변 판정 결과
 *
 * @param passed    키워드를 말했는지
 * @param attempts  이 질문에 답한 횟수
 * @param retryable 같은 질문에 한 번 더 답할 수 있는지
 * @param feedback  실패했을 때 보여줄 힌트. 통과면 {@code null}
 */
public record CallAnswerResponse(boolean passed, int attempts, boolean retryable,
                                 String feedback) {

}
