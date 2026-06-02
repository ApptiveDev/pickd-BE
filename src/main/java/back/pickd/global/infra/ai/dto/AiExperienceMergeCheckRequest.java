package back.pickd.global.infra.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiExperienceMergeCheckRequest {

    @Builder.Default
    private List<ExperiencePayload> targets = new ArrayList<>();

    @JsonProperty("existing_experiences")
    @Builder.Default
    private List<ExperiencePayload> existingExperiences = new ArrayList<>();

    private Double threshold;

    @JsonProperty("top_k")
    @Builder.Default
    private int topK = 1;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperiencePayload {
        private String id;

        private String title;

        @JsonProperty("experience_name")
        private String experienceName;

        @JsonProperty("experience_group")
        private String experienceGroup;

        @JsonProperty("experience_type")
        private String experienceType;

        @Builder.Default
        private List<String> keywords = new ArrayList<>();

        @Builder.Default
        private Map<String, Object> attributes = new HashMap<>();

        @JsonProperty("basic_info")
        @Builder.Default
        private Map<String, Object> basicInfo = new HashMap<>();

        @JsonProperty("document_content")
        private String documentContent;

        @JsonProperty("experience_content")
        private String experienceContent;
    }
}
