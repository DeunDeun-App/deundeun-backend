package kopo.deundeunbackend.common.response;

/**
 * API 공통 응답 템플릿
 *
 * @param success 성공 여부
 * @param code    화면 분기용 코드. 실패 시 원인을 구분
 * @param message 사용자에게 그대로 보여줄 수 있는 안내 문구
 * @param data    실제 결과. 실패 시 {@code null}
 */
public record ApiResponse<T>(boolean success, String code, String message, T data) {

    private static final String CODE_SUCCESS = "SUCCESS";

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, CODE_SUCCESS, message, data);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }
}
