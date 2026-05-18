package back.pickd.notice.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiSectionQualificationDto {
    @JsonProperty("general_qualification")
    private String generalQualification;

    @JsonProperty("mandatory_qualification")
    private String mandatoryQualification;

    @Builder
    public AiSectionQualificationDto(String generalQualification, String mandatoryQualification) {
        this.generalQualification = generalQualification;
        this.mandatoryQualification = mandatoryQualification;
    }
}
