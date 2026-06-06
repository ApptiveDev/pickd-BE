package back.pickd.global.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "공통 에러 응답")
@Getter
@Builder
public class ErrorResponse {
    @Schema(description = "에러 발생 시각", example = "2026-06-03T15:43:33.594")
    private final LocalDateTime timestamp;

    @Schema(description = "HTTP 상태 코드")
    private final int status;

    @Schema(description = "HTTP 상태 메시지")
    private final String error;

    @Schema(description = "에러 상세 메시지")
    private final String message;

    @Schema(description = "요청 경로")
    private final String path;
}
