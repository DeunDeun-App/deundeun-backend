package kopo.deundeunbackend.dto.response;

/**
 * 답변 판정 결과
 *
 * @param passed    필요한 단서를 모두 말했는지
 * @param attempts  이 질문에 답한 횟수
 * @param retryable 같은 질문에 한 번 더 답할 수 있는지
 * @param feedback  실패했을 때 보여줄 힌트. 통과했거나 되묻는 중이면 {@code null}
 * @param followUp  단서를 일부만 말했을 때 상담원이 되묻는 말. 없으면 {@code null}
 */
public record CallAnswerResponse(boolean passed, int attempts, boolean retryable,
                                 String feedback, String followUp) {

}
