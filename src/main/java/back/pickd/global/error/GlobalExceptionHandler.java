package back.pickd.global.error;

import back.pickd.experience.dto.ExperienceMergeDto.Conflict;
import back.pickd.experience.exception.ExperienceMergeConflictException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리기 (Global Exception Handler)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "요청하신 리소스를 찾을 수 없습니다: " + e.getResourcePath(), request);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException e, HttpServletRequest request) {
        log.warn("ApiException [{}]: {}", e.getStatus(), e.getMessage());
        return buildErrorResponse(e.getStatus(), e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", message);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler({IOException.class, GeneralSecurityException.class})
    public ResponseEntity<ErrorResponse> handleGoogleApiException(Exception e, HttpServletRequest request) {
        if (e instanceof ClientAbortException) {
            log.debug("Client aborted connection (broken pipe): {}", request.getRequestURI());
            return null;
        }
        log.error("Google API Exception: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "구글 API 연동 중 오류가 발생했습니다.", request);
    }

    @ExceptionHandler(ExperienceMergeConflictException.class)
    public ResponseEntity<Conflict> handleExperienceMergeConflict(ExperienceMergeConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getResponse());
    }

    @ExceptionHandler({OAuth2AuthenticationException.class, RuntimeException.class})
    public ResponseEntity<ErrorResponse> handleRuntimeException(Exception e, HttpServletRequest request) {
        log.error("Runtime Exception: {}", e.getMessage());
        String message = e.getMessage() != null ? e.getMessage() : "처리 중 오류가 발생했습니다.";
        HttpStatus status = message.contains("로그인") || message.contains("인증")
                ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
        return buildErrorResponse(status, message, request);
    }

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
