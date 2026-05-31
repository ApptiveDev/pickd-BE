package back.pickd.user.dto;

import back.pickd.user.entity.UserExperience;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ExperienceStep2SaveResult {
    private final List<UserExperience> savedExperiences;
    private final List<ExperienceMergeConflictResponse> mergeCandidates;
}
