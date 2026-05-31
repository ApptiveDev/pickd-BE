package back.pickd.notice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SectionQualificationSaveRequestDto {

    @NotBlank(message = "일반 지원 자격은 필수입니다.")
    private String generalQualification;

    private String mandatoryQualification;

    @Builder
    public SectionQualificationSaveRequestDto(String generalQualification, String mandatoryQualification) {
        this.generalQualification = generalQualification;
        this.mandatoryQualification = mandatoryQualification;
    }
}
