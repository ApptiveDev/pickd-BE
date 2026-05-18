package back.pickd.user.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiExperienceResponseDto {
    private List<AiExperienceDto> experiences;

    @Builder
    public AiExperienceResponseDto(List<AiExperienceDto> experiences) {
        this.experiences = experiences;
    }
}
