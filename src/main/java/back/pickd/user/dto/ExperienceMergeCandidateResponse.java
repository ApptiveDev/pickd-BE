package back.pickd.user.dto;

import back.pickd.user.entity.UserExperience;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExperienceMergeCandidateResponse {
    private final String id;
    private final String title;
    private final String experienceType;
    private final String experienceGroup;
    private final Double similarity;

    public static ExperienceMergeCandidateResponse from(UserExperience experience, Double similarity) {
        return new ExperienceMergeCandidateResponse(
                experience.getId(),
                experience.getTitle(),
                experience.getExperienceType() != null ? experience.getExperienceType().name() : null,
                experience.getExperienceGroup() != null ? experience.getExperienceGroup().name() : null,
                similarity
        );
    }
}
