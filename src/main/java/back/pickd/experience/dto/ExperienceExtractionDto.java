package back.pickd.experience.dto;

import back.pickd.experience.dto.ExperienceMergeDto.Conflict;
import back.pickd.experience.entity.ExperienceTemp;
import back.pickd.experience.entity.UserExperience;
import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.experience.enums.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ExperienceExtractionDto {

    private ExperienceExtractionDto() {
    }

    @Getter
    public static class TempResponse {
        private final Long id;
        private final Long userId;
        private final String experienceName;
        private final String experienceGroup;
        private final String experienceType;
        private final LocalDateTime createdAt;

        public TempResponse(ExperienceTemp temp) {
            this.id = temp.getId();
            this.userId = temp.getUser().getId();
            this.experienceName = temp.getExperienceName();
            this.experienceGroup = temp.getExperienceGroup() != null ? temp.getExperienceGroup().name() : null;
            this.experienceType = temp.getExperienceType() != null ? temp.getExperienceType().name() : null;
            this.createdAt = temp.getCreatedAt();
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Step2Response {
        private final List<ExperienceResponse> savedExperiences;
        private final List<Conflict> mergeCandidates;
    }

    @Getter
    @AllArgsConstructor
    public static class Step2SaveResult {
        private final List<UserExperience> savedExperiences;
        private final List<Conflict> mergeCandidates;
    }

    public enum Step3Action {
        CREATE_NEW,
        SKIP
    }

    @Getter
    @NoArgsConstructor
    public static class Step3Request {

        @NotEmpty(message = "처리할 중복 후보 결정 목록은 필수입니다.")
        private List<Decision> decisions = new ArrayList<>();
    }

    @Getter
    @NoArgsConstructor
    public static class Decision {

        @NotNull(message = "처리 action은 필수입니다.")
        private Step3Action action;

        @Valid
        @NotNull(message = "저장 후보 draft는 필수입니다.")
        private Draft draft;
    }

    @Getter
    @NoArgsConstructor
    public static class Draft {

        @NotBlank(message = "경험 제목은 필수입니다.")
        private String title;

        @NotNull(message = "경험 유형은 필수입니다.")
        private ExperienceType experienceType;

        @NotNull(message = "경험 그룹은 필수입니다.")
        private ExperienceGroup experienceGroup;

        private Status status = Status.COMPLETED;
        private String documentContent;
        private Map<String, Object> attributes = new HashMap<>();
        private List<String> keywords = new ArrayList<>();
    }

    @Getter
    @AllArgsConstructor
    public static class Step3Response {
        private final List<ExperienceResponse> savedExperiences;
        private final int skippedCount;
    }
}
