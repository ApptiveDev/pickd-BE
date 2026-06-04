package back.pickd.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

// URL 기반 채용공고 분석 요청 DTO
@Schema(description = "채용공고 URL 분석 요청")
@Getter
@NoArgsConstructor
public class UrlAnalysisRequestDto {

    @Schema(description = "AI가 분석할 채용공고 URL", example = "https://careers.example.com/jobs/123")
    @NotBlank(message = "URL은 필수입니다.")
    private String url;
}
