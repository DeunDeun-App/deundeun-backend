package kopo.deundeunbackend.dto.response;

/**
 * 훈련 1회의 키워드 하나. 완료 화면과 기록 카드에 ✓/✗ 로 보여준다
 *
 * @param feedback 못 했을 때 보여줄 한 줄. 해냈으면 null
 */
public record KeywordResponse(String label, boolean passed, String feedback) {

}
