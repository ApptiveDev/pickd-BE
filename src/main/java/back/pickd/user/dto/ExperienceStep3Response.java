package back.pickd.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ExperienceStep3Response {
    private final List<UserExperienceResponse> savedExperiences;
    private final int skippedCount;
}
