package back.pickd.user.dto;

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

@Getter
@AllArgsConstructor
public class ExperienceDraftResponse {
    private final String title;
    private final String experienceType;
    private final String experienceGroup;
    private final String status;
    private final String documentContent;
    private final Map<String, Object> attributes;
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
