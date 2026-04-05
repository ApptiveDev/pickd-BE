package back.pickd.config.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 프론트엔드에 전달할 공통 에러 응답 객체
 */
@Getter
@Builder
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
}
