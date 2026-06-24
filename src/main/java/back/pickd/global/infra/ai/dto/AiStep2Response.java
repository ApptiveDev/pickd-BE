package back.pickd.global.infra.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class AiStep2Response {
    private List<Step2ExperienceDto> experiences;

    @Getter
    @NoArgsConstructor
    public static class Step2ExperienceDto {
        @JsonProperty("experience_name")
        private String experience_name;

        @JsonProperty("experience_group")
        private String experience_group;

        @JsonProperty("experience_type")
        private String experience_type;

        private List<String> keywords;

        @JsonProperty("is_important")
        private boolean is_important;

        @JsonProperty("progress_status")
        private String progress_status;

        @JsonProperty("needs_merge")
        private boolean needs_merge;

        private boolean unanswered;

        @JsonProperty("has_ai_questions")
        private boolean has_ai_questions;

        // 11가지 소분류 유형에 따라 유동적인 필드는 Map으로 통째 파싱
        @JsonProperty("basic_info")
        private Map<String, Object> basic_info;

        @JsonProperty("experience_content")
        private String experience_content;

        @JsonProperty("tagged_body_text")
        private List<TaggedSentenceDto> tagged_body_text;

        @JsonProperty("document_editor_content")
        private String document_editor_content;

        @JsonProperty("related_links")
        private List<String> related_links;

        private List<String> attachments;

        @JsonProperty("ai_questions")
        private List<String> ai_questions;

        @JsonProperty("ai_sentence_cards")
        private List<String> ai_sentence_cards;

        @JsonProperty("merge_candidate_id")
        private String merge_candidate_id;

        @JsonProperty("merge_similarity")
        private Double merge_similarity;

        @JsonProperty("writing_status")
        private String writing_status;
    }

    @Getter
    @NoArgsConstructor
    public static class TaggedSentenceDto {
        private String tag;
        private String sentence;
    }
}
