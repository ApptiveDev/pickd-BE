package back.pickd.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Schema(description = "경험 추출 2단계 응답 (자동 저장 내역 및 중복 후보군)")
@Getter
@AllArgsConstructor
public class ExperienceStep2Response {

    @Schema(description = "중복이 없어 DB에 즉시 자동 저장된 경험 목록")
    private final List<UserExperienceResponse> savedExperiences;

    @Schema(description = "기존 저장된 경험과 유사도가 높아 승인을 보류한 중복 후보 목록")
    private final List<ExperienceMergeConflictResponse> mergeCandidates;
}
