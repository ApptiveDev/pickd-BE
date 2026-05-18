package back.pickd.notice.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiNoticeSectionDto {
    @JsonProperty("section_name")
    private String sectionName;

    @JsonProperty("job_title")
    private String jobTitle;

    private String responsibilities;
    private String workplace;
    private String headcount;

    private List<AiSectionQualificationDto> qualifications;
    private List<AiSectionPreferenceDto> preferences;

    @Builder
    public AiNoticeSectionDto(String sectionName, String jobTitle, String responsibilities, String workplace,
                              String headcount, List<AiSectionQualificationDto> qualifications,
                              List<AiSectionPreferenceDto> preferences) {
        this.sectionName = sectionName;
        this.jobTitle = jobTitle;
        this.responsibilities = responsibilities;
        this.workplace = workplace;
        this.headcount = headcount;
        this.qualifications = qualifications;
        this.preferences = preferences;
    }
}
