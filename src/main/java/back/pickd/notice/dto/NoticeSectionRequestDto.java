package back.pickd.notice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NoticeSectionRequestDto {

    @NotBlank(message = "섹션명은 필수입니다.")
    private String sectionName;

    @NotBlank(message = "직무명은 필수입니다.")
    private String jobTitle;

    private String responsibilities;
    private String headcount;
    private String workplace;
}
