package back.pickd.user.dto;

import back.pickd.user.entity.ExperienceTemp;
import lombok.Getter;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "임시 경험 추출 응답")
@Getter
public class ExperienceTempResponse {

    @Schema(description = "임시 경험 ID", example = "45")
    private final Long id;

    @Schema(description = "사용자 ID", example = "12")
    private final Long userId;

    @Schema(description = "추출된 경험명", example = "Apptive 24기 안드로이드 토이 프로젝트")
    private final String experienceName;

    @Schema(description = "경험 그룹 (대분류)", example = "NARRATIVE")
    private final String experienceGroup;

    @Schema(description = "경험 유형 (세부 분류)", example = "PROJECT")
    private final String experienceType;

    @Schema(description = "생성 시간")
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
