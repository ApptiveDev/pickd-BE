package back.pickd.user.dto.ai;

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
public class AiExperienceFileResultDto {
    private String status;

    @JsonProperty("extracted_count")
    private Integer extractedCount;

    private List<AiExperienceDto> experiences;

    @Builder
    public AiExperienceFileResultDto(String status, Integer extractedCount, List<AiExperienceDto> experiences) {
        this.status = status;
        this.extractedCount = extractedCount;
        this.experiences = experiences;
    }
}
