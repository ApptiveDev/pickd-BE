package back.pickd.experience.dto;

import back.pickd.experience.entity.ExperienceExtractionBatch;
import back.pickd.experience.entity.ExperienceExtractionDraft;
import back.pickd.experience.entity.ExperienceTemp;
import back.pickd.experience.entity.UserExperience;
import back.pickd.experience.support.PresetRegistry.PresetField;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

public final class ExperienceExtractionTestDto {

    private ExperienceExtractionTestDto() {
    }

    public record PresetDefinition(
            String type,
            String koreanName,
            String group,
            List<PresetField> fields
    ) {
    }

    public record StateResponse(
            String userEmail,
            List<ExperienceResponse> experiences,
            List<TempState> temps,
            List<BatchState> batches
    ) {
    }

    public record TempState(
            Long id,
            String experienceName,
            String experienceGroup,
            String experienceType,
            String resumeUrl,
            LocalDateTime createdAt
    ) {
        public static TempState from(ExperienceTemp temp) {
            return new TempState(
                    temp.getId(),
                    temp.getExperienceName(),
                    temp.getExperienceGroup() != null ? temp.getExperienceGroup().name() : null,
                    temp.getExperienceType() != null ? temp.getExperienceType().name() : null,
                    temp.getResumeUrl(),
                    temp.getCreatedAt()
            );
        }
    }

    public record BatchState(
            String id,
            String status,
            OffsetDateTime createdAt,
            OffsetDateTime processedAt,
            List<DuplicateGroupState> duplicateGroups
    ) {
        public static BatchState from(ExperienceExtractionBatch batch) {
            return new BatchState(
                    batch.getId(),
                    batch.getStatus().name(),
                    batch.getCreatedAt(),
                    batch.getProcessedAt(),
                    batch.getGroups().stream()
                            .map(DuplicateGroupState::from)
                            .toList()
            );
        }
    }

    public record DuplicateGroupState(
            String id,
            ExperienceResponse existingExperience,
            List<DraftState> drafts
    ) {
        public static DuplicateGroupState from(
                back.pickd.experience.entity.ExperienceDuplicateGroup group
        ) {
            return new DuplicateGroupState(
                    group.getId(),
                    new ExperienceResponse(group.getExistingExperience()),
                    group.getDrafts().stream()
                            .map(DraftState::from)
                            .toList()
            );
        }
    }

    public record DraftState(
            String id,
            String title,
            String experienceType,
            String experienceGroup,
            String status,
            String documentContent,
            java.util.Map<String, Object> attributes,
            List<String> keywords,
            String resumeUrl,
            Double similarity
    ) {
        public static DraftState from(ExperienceExtractionDraft draft) {
            return new DraftState(
                    draft.getId(),
                    draft.getTitle(),
                    draft.getExperienceType().name(),
                    draft.getExperienceGroup().name(),
                    draft.getStatus().name(),
                    draft.getDocumentContent(),
                    draft.getAttributes(),
                    draft.getKeywords(),
                    draft.getResumeUrl(),
                    draft.getSimilarity()
            );
        }
    }
}
