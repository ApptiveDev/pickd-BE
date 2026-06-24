package back.pickd.global.infra.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class AiStep1Response {
    private List<ExperienceSummaryDto> experiences;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperienceSummaryDto {
        @JsonProperty("experience_name")
        private String experience_name;

        @JsonProperty("experience_group")
        private String experience_group;

        @JsonProperty("experience_type")
        private String experience_type;
    }
}
