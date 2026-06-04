package back.pickd.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 경험 생성 응답 DTO
@Schema(description = "경험 생성 응답")
@Getter
@AllArgsConstructor
public class ExperienceCreateResponseDto {
    @Schema(description = "생성된 경험 ID", example = "exp_7f8a9b2c")
    private final String id;
}
