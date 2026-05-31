package back.pickd.notice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

// URL 기반 채용공고 분석 요청 DTO
@Getter
@NoArgsConstructor
public class UrlAnalysisRequestDto {

    @NotBlank(message = "URL은 필수입니다.")
    private String url;
}
