package back.pickd.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "경험 중복 충돌 세부 정보")
@Getter
@AllArgsConstructor
public class ExperienceMergeConflictResponse {

    @Schema(description = "병합/중복 판정 처리 필요 여부", example = "true")
    private final boolean needsMerge;

    @Schema(description = "이미 DB에 저장되어 있는 유사 경험 카드 정보")
    private final ExperienceMergeCandidateResponse candidate;

    @Schema(description = "기존 저장된 데이터와의 유사도 비율 (0.0 ~ 1.0)", example = "0.87")
    private final Double similarity;

    @Schema(description = "이번에 AI가 분석/작성한 신규 경험 초안(Draft) 정보")
    private final ExperienceDraftResponse draft;
}
