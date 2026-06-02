package back.pickd.global.infra.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AiExperienceMergeCheckResponse {
    private List<MergeCheckResult> results;

    @Getter
    @NoArgsConstructor
    public static class MergeCheckResult {
        @JsonProperty("target_index")
        private int targetIndex;

        @JsonProperty("target_id")
        private String targetId;

        @JsonProperty("needs_merge")
        private boolean needsMerge;

        @JsonProperty("merge_candidate_id")
        private String mergeCandidateId;

        private Double similarity;

        private MergeCandidate candidate;
    }

    @Getter
    @NoArgsConstructor
    public static class MergeCandidate {
        private String id;
        private String title;

        @JsonProperty("experience_group")
        private String experienceGroup;

        @JsonProperty("experience_type")
        private String experienceType;

        private Double similarity;
    }
}
