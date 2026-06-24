package back.pickd.global.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "공통 오류 응답")
public class ErrorResponse {

    @Schema(description = "오류 발생 시각", example = "2026-06-20T17:20:00")
    private final LocalDateTime timestamp;

    @Schema(description = "HTTP 상태 코드", example = "400")
    private final int status;

    @Schema(description = "HTTP 오류 이름", example = "Bad Request")
    private final String error;

    @Schema(description = "오류 상세 메시지", example = "batch의 모든 중복 그룹에 대한 선택이 필요합니다.")
    private final String message;

    @Schema(description = "요청 경로", example = "/api/v2/experiences/extract/step3")
    private final String path;
}
