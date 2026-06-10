package back.pickd.experience.dto;

import back.pickd.experience.dto.ExperienceCreateDto.Request;
import back.pickd.experience.entity.UserExperience;
import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.experience.enums.Status;
import back.pickd.global.infra.ai.dto.AiStep2Response;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ExperienceMergeDto {

    private ExperienceMergeDto() {
    }

    @Getter
    @AllArgsConstructor
    public static class Draft {
        private final String title;
        private final String experienceType;
        private final String experienceGroup;
        private final String status;
        private final String documentContent;
        private final Map<String, Object> attributes;
        private final List<String> keywords;

        public static Draft fromCreateRequest(Request request) {
            return new Draft(
                    request.getTitle(),
                    request.getExperienceType() != null ? request.getExperienceType().name() : null,
                    request.getExperienceGroup() != null ? request.getExperienceGroup().name() : null,
                    request.getStatus() != null ? request.getStatus().name() : null,
                    request.getDocumentContent(),
                    request.getAttributes() != null ? request.getAttributes() : new HashMap<>(),
                    request.getKeywords() != null ? request.getKeywords() : new ArrayList<>()
            );
        }

        public static Draft fromStep2(
                AiStep2Response.Step2ExperienceDto dto,
                ExperienceType type,
                ExperienceGroup group,
                Status status
        ) {
            return new Draft(
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

    @Getter
    @AllArgsConstructor
    public static class Candidate {
        private final String id;
        private final String title;
        private final String experienceType;
        private final String experienceGroup;
        private final Double similarity;

        public static Candidate from(UserExperience experience, Double similarity) {
            return new Candidate(
                    experience.getId(),
                    experience.getTitle(),
                    experience.getExperienceType() != null
                            ? experience.getExperienceType().name()
                            : null,
                    experience.getExperienceGroup() != null
                            ? experience.getExperienceGroup().name()
                            : null,
                    similarity
            );
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Conflict {
        private final boolean needsMerge;
        private final Candidate candidate;
        private final Double similarity;
        private final Draft draft;
    }
}
