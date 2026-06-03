package back.pickd.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Schema(description = "경험 추출 3단계 결정 처리 완료 응답")
@Getter
@AllArgsConstructor
public class ExperienceStep3Response {

    @Schema(description = "사용자의 결정을 거쳐 최종 신규 저장 완료된 경험 리스트")
    private final List<UserExperienceResponse> savedExperiences;

    @Schema(description = "사용자가 스킵(SKIP)하여 저장을 건너뛴 경험 개수", example = "1")
    private final int skippedCount;
}
