package kopo.deundeunbackend.dto.request;

/**
 * 119 신고 연습 답변. 회원은 로그인 토큰으로 확인한다
 *
 * @param itemId 상담원 질문 번호
 * @param answer 음성 인식 결과
 */
public record CallRequest(Long itemId, String answer) {

}
