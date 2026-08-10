package kopo.deundeunbackend.common.exception;

import kopo.deundeunbackend.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 모든 컨트롤러에서 발생한 예외를 공통 응답 형태로 변환
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException e) {
        log.warn("업무 예외 발생: code={}, message={}", e.getCode(), e.getMessage());

        return ResponseEntity.status(e.getStatus())
                .body(ApiResponse.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("요청 본문 해석 실패: {}", e.getMessage());

        return ResponseEntity.badRequest()
                .body(ApiResponse.fail("INVALID_REQUEST", "요청 형식이 올바르지 않습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        log.error("처리되지 않은 예외", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."));
    }
}
