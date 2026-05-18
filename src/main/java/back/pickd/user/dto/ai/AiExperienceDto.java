package back.pickd.user.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiExperienceDto {
    @JsonProperty("experience_name")
    private String experienceName;

    @JsonProperty("experience_type")
    private String experienceType;

    private String organization;
    private String period;

    @JsonProperty("my_role")
    private String myRole;

    private String situation;
    private String action;
    private String result;
    private String learnings;

    @JsonProperty("core_competencies")
    private List<String> coreCompetencies;

    @JsonProperty("applicable_questions")
    private List<String> applicableQuestions;

    @JsonProperty("source_text")
    private String sourceText;

    private String status;

    @Builder
    public AiExperienceDto(String experienceName, String experienceType, String organization, String period,
                           String myRole, String situation, String action, String result, String learnings,
                           List<String> coreCompetencies, List<String> applicableQuestions, String sourceText,
                           String status) {
        this.experienceName = experienceName;
        this.experienceType = experienceType;
        this.organization = organization;
        this.period = period;
        this.myRole = myRole;
        this.situation = situation;
        this.action = action;
        this.result = result;
        this.learnings = learnings;
        this.coreCompetencies = coreCompetencies;
        this.applicableQuestions = applicableQuestions;
        this.sourceText = sourceText;
        this.status = status;
    }
}
