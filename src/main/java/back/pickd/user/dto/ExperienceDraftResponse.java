package back.pickd.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import back.pickd.global.infra.ai.dto.AiStep2Response;
import back.pickd.user.entity.enums.ExperienceGroup;
import back.pickd.user.entity.enums.ExperienceType;
import back.pickd.user.entity.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Schema(description = "이번에 신규 추출된 경험 초안(Draft) 정보")
@Getter
@AllArgsConstructor
public class ExperienceDraftResponse {

    @Schema(description = "경험 제목", example = "Apptive 24기 안드로이드 토이 프로젝트")
    private final String title;

    @Schema(description = "경험 유형 (PROJECT, ACTIVITY 등)", example = "PROJECT")
    private final String experienceType;

    @Schema(description = "경험 분류 (NARRATIVE, SPEC)", example = "NARRATIVE")
    private final String experienceGroup;

    @Schema(description = "경험 진행 여부 상태 (IN_PROGRESS, COMPLETED)", example = "COMPLETED")
    private final String status;

    @Schema(description = "경험 수기 본문 (STAR-L 텍스트 등)", example = "[S] 동아리 프로젝트를 시작함... [T] ... [A] ... [R] ... [L] ...")
    private final String documentContent;

    @Schema(description = "기타 스펙용 속성 데이터")
    private final Map<String, Object> attributes;

    @Schema(description = "추출된 핵심 키워드 리스트")
    private final List<String> keywords;

    public static ExperienceDraftResponse fromCreateRequest(ExperienceCreateRequestDto request) {
        return new ExperienceDraftResponse(
                request.getTitle(),
                request.getExperienceType() != null ? request.getExperienceType().name() : null,
                request.getExperienceGroup() != null ? request.getExperienceGroup().name() : null,
                request.getStatus() != null ? request.getStatus().name() : null,
                request.getDocumentContent(),
                request.getAttributes() != null ? request.getAttributes() : new HashMap<>(),
                request.getKeywords() != null ? request.getKeywords() : new ArrayList<>()
        );
    }

    public static ExperienceDraftResponse fromStep2(
            AiStep2Response.Step2ExperienceDto dto,
            ExperienceType type,
            ExperienceGroup group,
            Status status
    ) {
        return new ExperienceDraftResponse(
                dto.getExperience_name(),
                type != null ? type.name() : null,
                group != null ? group.name() : null,
                status != null ? status.name() : null,
                dto.getExperience_content(),
                dto.getBasic_info() != null ? dto.getBasic_info() : new HashMap<>(),
                dto.getKeywords() != null ? dto.getKeywords() : new ArrayList<>()
        );
    }
}
