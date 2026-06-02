package back.pickd.global.infra.ai.dto;

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
        private String experience_name;
        private String experience_group;
        private String experience_type;
        private List<String> keywords;
        private boolean is_important;
        private String progress_status;
        private boolean needs_merge;
        private boolean unanswered;
        private boolean has_ai_questions;
        
        // 11가지 소분류 유형에 따라 유동적인 필드는 Map으로 통째 파싱
        private Map<String, Object> basic_info;
        
        private String experience_content;
        private List<TaggedSentenceDto> tagged_body_text;
        private String document_editor_content;
        private List<String> related_links;
        private List<String> attachments;
        private List<String> ai_questions;
        private List<String> ai_sentence_cards;
        private String merge_candidate_id;
        private Double merge_similarity;
        private String writing_status;
    }

    @Getter
    @NoArgsConstructor
    public static class TaggedSentenceDto {
        private String tag;
        private String sentence;
    }
}
