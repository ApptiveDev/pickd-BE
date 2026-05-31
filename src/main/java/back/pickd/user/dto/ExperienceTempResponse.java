package back.pickd.user.dto;

import back.pickd.user.entity.ExperienceTemp;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ExperienceTempResponse {

    private final Long id;
    private final Long userId;
    private final String experienceName;
    private final String experienceGroup;
    private final String experienceType;
    private final LocalDateTime createdAt;

    public ExperienceTempResponse(ExperienceTemp temp) {
        this.id = temp.getId();
        this.userId = temp.getUser().getId();
        this.experienceName = temp.getExperienceName();
        this.experienceGroup = temp.getExperienceGroup();
        this.experienceType = temp.getExperienceType();
        this.createdAt = temp.getCreatedAt();
    }
}
