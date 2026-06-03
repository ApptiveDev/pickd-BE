package back.pickd.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import back.pickd.user.entity.UserExperience;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "이미 DB에 보관중인 유사 경험 정보")
@Getter
@AllArgsConstructor
public class ExperienceMergeCandidateResponse {

    @Schema(description = "기존 경험의 DB 고유 ID", example = "exp_7f8a9b2c...")
    private final String id;

    @Schema(description = "기존 경험의 제목", example = "앱티브 연합 동아리 안드로이드 개발")
    private final String title;

    @Schema(description = "경험 유형 (PROJECT, ACTIVITY 등)", example = "PROJECT")
    private final String experienceType;

    @Schema(description = "경험 분류 (NARRATIVE, SPEC)", example = "NARRATIVE")
    private final String experienceGroup;

    @Schema(description = "유사성 백분율 점수", example = "0.87")
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
