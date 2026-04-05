package back.pickd.config.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;

/**
 * 전역 예외 처리기 (Global Exception Handler)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 리소스(정적 파일 등)를 찾을 수 없는 경우 처리 (예: favicon.ico)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        // favicon 에러 등은 로그를 남기지 않거나 아주 짧게만 남깁니다.
        return buildErrorResponse(HttpStatus.NOT_FOUND, "요청하신 리소스를 찾을 수 없습니다: " + e.getResourcePath(), request);
    }

    /**
     * 구글 API 호출 중 발생할 수 있는 보안 및 IO 예외 처리
     */
    @ExceptionHandler({IOException.class, GeneralSecurityException.class})
    public ResponseEntity<ErrorResponse> handleGoogleApiException(Exception e, HttpServletRequest request) {
        log.error("Google API Exception: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "구글 API 연동 중 오류가 발생했습니다.", request);
    }

    /**
     * 인증 관련 예외 처리
     */
    @ExceptionHandler({OAuth2AuthenticationException.class, RuntimeException.class})
    public ResponseEntity<ErrorResponse> handleAuthException(Exception e, HttpServletRequest request) {
        log.error("Authentication/Runtime Exception: {}", e.getMessage());
        
        HttpStatus status = e.getMessage().contains("로그인") || e.getMessage().contains("인증") 
                ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
        return buildErrorResponse(status, e.getMessage(), request);
    }

    /**
     * 나머지 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception e, HttpServletRequest request) {
        log.error("Unhandled Exception: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 서버 내부 오류가 발생했습니다.", request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(response, status);
    }
}