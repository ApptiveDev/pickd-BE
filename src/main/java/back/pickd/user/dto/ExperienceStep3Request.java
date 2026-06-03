package back.pickd.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "경험 추출 3단계 사용자 최종 병합/저장 결정 요청")
@Getter
@NoArgsConstructor
public class ExperienceStep3Request {

    @Schema(description = "보류되었던 중복 경험들에 대한 사용자 결정(저장/스킵) 목록")
    @NotEmpty(message = "처리할 중복 후보 결정 목록은 필수입니다.")
    private List<Decision> decisions = new ArrayList<>();

    @Schema(description = "개별 경험에 대한 사용자의 조치 결정 정보")
    @Getter
    @NoArgsConstructor
    public static class Decision {

        @Schema(description = "최종 처리 방식 (CREATE_NEW: 신규 저장, SKIP: 저장 안 함)", example = "CREATE_NEW")
        @NotNull(message = "처리 action은 필수입니다.")
        private ExperienceStep3Action action;

        @Schema(description = "최종 결정 대상인 경험 카드 초안 데이터")
        @Valid
        @NotNull(message = "저장 후보 draft는 필수입니다.")
        private ExperienceDraftRequest draft;
    }
}
