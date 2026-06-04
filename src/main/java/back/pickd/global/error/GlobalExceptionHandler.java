package back.pickd.global.error;

import back.pickd.user.dto.ExperienceMergeConflictResponse;
import back.pickd.user.exception.ExperienceMergeConflictException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리기 (Global Exception Handler)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException e, HttpServletRequest request) {
        if (e.getStatus().is5xxServerError()) {
            log.error("API Exception: {}", e.getMessage(), e);
        } else {
            log.warn("API Exception: {}", e.getMessage());
        }
        return buildErrorResponse(e.getStatus(), e.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "요청하신 리소스를 찾을 수 없습니다: " + e.getResourcePath(), request);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class
    })
    public ResponseEntity<ErrorResponse> handleValidationException(Exception e, HttpServletRequest request) {
        String message;
        if (e instanceof MethodArgumentNotValidException validationException) {
            message = validationException.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
        } else if (e instanceof BindException bindException) {
            message = bindException.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
        } else {
            message = "요청값이 올바르지 않습니다.";
        }
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message.isBlank() ? "요청값이 올바르지 않습니다." : message, request);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MultipartException.class,
            HttpMediaTypeNotSupportedException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestException(Exception e, HttpServletRequest request) {
        log.warn("Bad Request Exception: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, "업로드 가능한 파일 크기를 초과했습니다.", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("Authentication Exception: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("Access Denied Exception: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", request);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.", request);
    }

    @ExceptionHandler({IOException.class, GeneralSecurityException.class})
    public ResponseEntity<ErrorResponse> handleGoogleApiException(Exception e, HttpServletRequest request) {
        log.error("Google API Exception: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, "구글 API 연동 중 오류가 발생했습니다.", request);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClientException(RestClientException e, HttpServletRequest request) {
        log.error("External API Exception: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, "외부 API 연동 중 오류가 발생했습니다.", request);
    }

    @ExceptionHandler(ExperienceMergeConflictException.class)
    public ResponseEntity<ExperienceMergeConflictResponse> handleExperienceMergeConflict(ExperienceMergeConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getResponse());
    }

    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleOAuth2AuthenticationException(OAuth2AuthenticationException e, HttpServletRequest request) {
        log.warn("OAuth2 Authentication Exception: {}", e.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "OAuth2 인증에 실패했습니다.", request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("Unhandled Runtime Exception: {}", e.getMessage(), e);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", request);
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
