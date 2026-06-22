package back.pickd.experience.dto;

import back.pickd.experience.entity.ExperienceDuplicateGroup;
import back.pickd.experience.entity.ExperienceExtractionDraft;
import back.pickd.experience.entity.UserExperience;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.OffsetDateTime;

public final class ExperienceExtractionV2Dto {

    private ExperienceExtractionV2Dto() {
    }

    @Getter
    @NoArgsConstructor
    @Schema(name = "ExperienceExtractionV2Step2Request")
    public static class Step2Request {

        @NotEmpty(message = "선택된 임시 경험 ID는 필수입니다.")
        @Schema(
                description = "Step1에서 사용자가 선택한 임시 경험 ID. 배열 순서가 배치 내부 중복 판정 우선순위입니다.",
                example = "[12, 15, 18]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private List<Long> selectedTempIds = new ArrayList<>();
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "ExperienceExtractionV2Step2Response")
    public static class Step2Response {

        @Schema(description = "중복이 아니어서 Step2에서 즉시 저장된 경험")
        private final List<ExperienceResponse> savedExperiences;

        @Schema(
                description = "Step3 처리에 사용할 중복 batch ID. 중복 후보가 없으면 null",
                example = "f128a54c-0525-4d89-96bb-ce8f7eb602b3",
                nullable = true
        )
        private final String duplicateBatchId;

        @Schema(description = "기존 경험과 추출 draft를 묶은 중복 후보 그룹")
        private final List<DuplicateGroupResponse> duplicateGroups;
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "ExperienceExtractionV2PendingBatchesResponse")
    public static class PendingBatchesResponse {

        @Schema(description = "현재 사용자의 미처리 중복 batch. 최신 생성 순으로 반환됩니다.")
        private final List<PendingBatchResponse> batches;
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "ExperienceExtractionV2PendingBatch")
    public static class PendingBatchResponse {

        @Schema(
                description = "Step3 duplicateBatchId로 전달할 미처리 batch ID",
                example = "f128a54c-0525-4d89-96bb-ce8f7eb602b3"
        )
        private final String duplicateBatchId;

        @Schema(description = "중복 batch 생성 시각")
        private final OffsetDateTime createdAt;

        @Schema(description = "Step3에서 선택할 기존 경험과 추출 draft 그룹")
        private final List<DuplicateGroupResponse> duplicateGroups;

        public static PendingBatchResponse from(
                back.pickd.experience.entity.ExperienceExtractionBatch batch
        ) {
            return new PendingBatchResponse(
                    batch.getId(),
                    batch.getCreatedAt(),
                    batch.getGroups().stream()
                            .map(DuplicateGroupResponse::from)
                            .toList()
            );
        }
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "ExperienceExtractionV2DuplicateGroup")
    public static class DuplicateGroupResponse {

        @Schema(
                description = "Step3 그룹 선택에 사용하는 중복 그룹 ID",
                example = "9f83540e-5d78-4a08-a284-78cecfdf2e3d"
        )
        private final String groupId;

        @Schema(description = "그룹에서 선택 가능한 기존 경험 및 추출 draft")
        private final List<DuplicateItemResponse> items;

        public static DuplicateGroupResponse from(ExperienceDuplicateGroup group) {
            List<DuplicateItemResponse> items = new ArrayList<>();
            items.add(DuplicateItemResponse.fromExisting(group.getExistingExperience()));
            group.getDrafts().stream()
                    .map(DuplicateItemResponse::fromDraft)
                    .forEach(items::add);
            return new DuplicateGroupResponse(group.getId(), items);
        }
    }

    @Schema(description = "중복 후보 데이터의 출처")
    public enum DuplicateItemSource {
        EXISTING,
        EXTRACTED
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "ExperienceExtractionV2DuplicateItem")
    public static class DuplicateItemResponse {

        @Schema(
                description = "Step3 selectedItemIds에 사용할 ID. 기존 경험이면 experience ID, 추출 후보이면 draft ID",
                example = "6d5d36a9-9856-4117-ac58-dd1254e28e75"
        )
        private final String itemId;

        @Schema(
                description = "기존 저장 경험 또는 새 추출 draft",
                allowableValues = {"EXISTING", "EXTRACTED"},
                example = "EXTRACTED"
        )
        private final DuplicateItemSource source;

        @Schema(
                description = "추출 draft와 기준 경험의 유사도. 기존 경험 항목은 null",
                example = "0.91",
                nullable = true
        )
        private final Double similarity;

        @Schema(description = "비교 화면에 표시할 경험 정보")
        private final ExperienceSnapshot experience;

        public static DuplicateItemResponse fromExisting(UserExperience experience) {
            return new DuplicateItemResponse(
                    experience.getId(),
                    DuplicateItemSource.EXISTING,
                    null,
                    ExperienceSnapshot.from(experience)
            );
        }

        public static DuplicateItemResponse fromDraft(ExperienceExtractionDraft draft) {
            return new DuplicateItemResponse(
                    draft.getId(),
                    DuplicateItemSource.EXTRACTED,
                    draft.getSimilarity(),
                    ExperienceSnapshot.from(draft)
            );
        }
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "ExperienceExtractionV2Snapshot")
    public static class ExperienceSnapshot {

        @Schema(description = "경험 제목", example = "금융 AI Agent 프로젝트")
        private final String title;

        @Schema(description = "경험 유형 enum", example = "PROJECT")
        private final String experienceType;

        @Schema(
                description = "경험 대분류 enum",
                allowableValues = {"NARRATIVE", "SPEC"},
                example = "NARRATIVE"
        )
        private final String experienceGroup;

        @Schema(
                description = "경험 작성 상태",
                allowableValues = {"IN_PROGRESS", "COMPLETED"},
                example = "COMPLETED"
        )
        private final String status;

        @Schema(description = "추출된 경험 본문", example = "추천 모델 개선을 담당했습니다.")
        private final String documentContent;

        @Schema(
                description = "PresetRegistry 기준 유형별 속성",
                example = "{\"project_name\":\"금융 AI Agent\",\"role\":\"백엔드 개발\"}"
        )
        private final Map<String, Object> attributes;

        @Schema(description = "경험 역량 키워드", example = "[\"문제 해결\", \"실행력\"]")
        private final List<String> keywords;

        public static ExperienceSnapshot from(UserExperience experience) {
            return new ExperienceSnapshot(
                    experience.getTitle(),
                    experience.getExperienceType().name(),
                    experience.getExperienceGroup().name(),
                    experience.getStatus().name(),
                    experience.getDocumentContent(),
                    experience.getAttributes() != null ? experience.getAttributes() : new HashMap<>(),
                    experience.getKeywords() != null ? experience.getKeywords() : new ArrayList<>()
            );
        }

        public static ExperienceSnapshot from(ExperienceExtractionDraft draft) {
            return new ExperienceSnapshot(
                    draft.getTitle(),
                    draft.getExperienceType().name(),
                    draft.getExperienceGroup().name(),
                    draft.getStatus().name(),
                    draft.getDocumentContent(),
                    draft.getAttributes(),
                    draft.getKeywords()
            );
        }
    }

    @Getter
    @NoArgsConstructor
    @Schema(name = "ExperienceExtractionV2Step3Request")
    public static class Step3Request {

        @NotBlank(message = "중복 batch ID는 필수입니다.")
        @Schema(
                description = "Step2에서 반환된 duplicateBatchId",
                example = "f128a54c-0525-4d89-96bb-ce8f7eb602b3",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String duplicateBatchId;

        @Valid
        @NotEmpty(message = "중복 그룹 선택 목록은 필수입니다.")
        @Schema(
                description = "batch의 모든 중복 그룹에 대한 선택. 그룹 누락은 허용되지 않습니다.",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private List<GroupSelection> groups = new ArrayList<>();
    }

    @Getter
    @NoArgsConstructor
    @Schema(name = "ExperienceExtractionV2GroupSelection")
    public static class GroupSelection {

        @NotBlank(message = "중복 group ID는 필수입니다.")
        @Schema(
                description = "Step2 duplicateGroups의 groupId",
                example = "9f83540e-5d78-4a08-a284-78cecfdf2e3d",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private String groupId;

        @NotEmpty(message = "그룹마다 하나 이상의 경험을 선택해야 합니다.")
        @Schema(
                description = "최종적으로 남길 itemId 목록. 기존 경험과 여러 draft를 함께 선택할 수 있습니다.",
                example = "[\"existing-experience-id\", \"draft-id\"]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private List<String> selectedItemIds = new ArrayList<>();
    }

    @Getter
    @AllArgsConstructor
    @Schema(name = "ExperienceExtractionV2Step3Response")
    public static class Step3Response {

        @Schema(description = "Step3 처리 후 최종적으로 유지되거나 신규 저장된 경험")
        private final List<ExperienceResponse> selectedExperiences;

        @Schema(
                description = "선택되지 않아 삭제된 기존 경험 ID",
                example = "[\"deleted-existing-experience-id\"]"
        )
        private final List<String> deletedExperienceIds;
    }
}
