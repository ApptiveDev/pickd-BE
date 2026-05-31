package back.pickd.global.infra.ai.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AiNoticeSectionDto {
    private String sectionName;
    private String jobTitle;
    private String responsibilities;
    private String workplace;
    private String headcount;
    private List<AiSectionQualificationDto> qualifications;
    private List<AiSectionPreferenceDto> preferences;
}
