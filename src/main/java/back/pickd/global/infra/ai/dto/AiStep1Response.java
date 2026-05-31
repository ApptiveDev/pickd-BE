package back.pickd.global.infra.ai.dto;

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
        private String experience_name;
        private String experience_group;
        private String experience_type;
    }
}
