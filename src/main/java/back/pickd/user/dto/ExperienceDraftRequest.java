package back.pickd.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import back.pickd.user.entity.enums.ExperienceGroup;
import back.pickd.user.entity.enums.ExperienceType;
import back.pickd.user.entity.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Schema(description = "최종 결정을 통과하여 저장할 경험 초안 데이터")
@Getter
@NoArgsConstructor
public class ExperienceDraftRequest {

    @Schema(description = "경험 카드 제목", example = "Apptive 24기 안드로이드 토이 프로젝트")
    @NotBlank(message = "경험 제목은 필수입니다.")
    private String title;

    @Schema(description = "경험 유형 (PROJECT, ACTIVITY 등)", example = "PROJECT")
    @NotNull(message = "경험 유형은 필수입니다.")
    private ExperienceType experienceType;

    @Schema(description = "경험 분류 (NARRATIVE, SPEC)", example = "NARRATIVE")
    @NotNull(message = "경험 그룹은 필수입니다.")
    private ExperienceGroup experienceGroup;

    @Schema(description = "경험 상태 (IN_PROGRESS, COMPLETED)", example = "COMPLETED")
    private Status status = Status.COMPLETED;

    @Schema(description = "경험 수기 본문 (STAR-L 텍스트 등)", example = "[S] 동아리 프로젝트를 시작함... [T] ... [A] ... [R] ... [L] ...")
    private String documentContent;

    @Schema(description = "기타 스펙용 속성 데이터")
    private Map<String, Object> attributes = new HashMap<>();

    @Schema(description = "경험 키워드 리스트")
    private List<String> keywords = new ArrayList<>();
}
