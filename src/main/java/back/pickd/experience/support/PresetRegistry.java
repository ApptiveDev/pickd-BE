package back.pickd.experience.support;

import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.global.infra.ai.dto.AiExperiencePresetSchema;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PresetRegistry {

    public record PresetField(String key, String label) {
    }

    private static final Map<ExperienceType, List<PresetField>> PRESET_MAP =
            new EnumMap<>(ExperienceType.class);

    private static final Set<ExperienceType> SPEC_TYPES = EnumSet.of(
            ExperienceType.LANGUAGE,
            ExperienceType.LICENSE,
            ExperienceType.AWARD,
            ExperienceType.COURSE,
            ExperienceType.EDUCATION
    );

    static {
        // 상세 서술형 (Narrative) 프리셋 필드 정의
        PRESET_MAP.put(ExperienceType.PROJECT, List.of(
                new PresetField("project_name", "프로젝트명"),
                new PresetField("period", "진행 기간"),
                new PresetField("role", "역할"),
                new PresetField("organization", "소속/팀"),
                new PresetField("achievements", "주요 성과")
        ));
        PRESET_MAP.put(ExperienceType.ACTIVITY, List.of(
                new PresetField("activity_name", "활동명"),
                new PresetField("organization", "주관기관"),
                new PresetField("period", "활동 기간"),
                new PresetField("role", "역할"),
                new PresetField("achievements", "주요 성과")
        ));
        PRESET_MAP.put(ExperienceType.INTERN, List.of(
                new PresetField("organization", "회사/기관명"),
                new PresetField("department", "직무/부서"),
                new PresetField("period", "근무/참여 기간"),
                new PresetField("task", "담당 업무"),
                new PresetField("achievements", "주요 성과")
        ));
        PRESET_MAP.put(ExperienceType.CONTEST, List.of(
                new PresetField("competition_name", "공모전명"),
                new PresetField("organization", "주관기관"),
                new PresetField("period", "참가 기간"),
                new PresetField("role", "역할"),
                new PresetField("achievements", "수상/결과")
        ));
        PRESET_MAP.put(ExperienceType.VOLUNTEER, List.of(
                new PresetField("activity_name", "활동명"),
                new PresetField("organization", "기관/단체"),
                new PresetField("period", "활동 기간"),
                new PresetField("role", "역할")
        ));
        PRESET_MAP.put(ExperienceType.EXCHANGE, List.of(
                new PresetField("location", "국가/도시"),
                new PresetField("organization", "학교명"),
                new PresetField("period", "파견 기간"),
                new PresetField("major", "전공/수강 분야")
        ));
        PRESET_MAP.put(ExperienceType.ALBA, List.of(
                new PresetField("workplace_name", "근무처명"),
                new PresetField("period", "근무 기간"),
                new PresetField("work_type", "업무 유형"),
                new PresetField("task", "담당 업무"),
                new PresetField("key_experience", "주요 경험")
        ));
        PRESET_MAP.put(ExperienceType.RESEARCH, List.of(
                new PresetField("lab_name", "연구실명"),
                new PresetField("organization", "소속 기관"),
                new PresetField("period", "참여 기간"),
                new PresetField("research_topic", "연구 주제"),
                new PresetField("role", "담당 역할"),
                new PresetField("deliverables", "주요 결과물")
        ));

        // 스펙·증빙 (Spec) 프리셋 필드 정의
        PRESET_MAP.put(ExperienceType.LANGUAGE, List.of(
                new PresetField("exam_name", "시험명"),
                new PresetField("score", "점수/등급"),
                new PresetField("exam_date", "응시일"),
                new PresetField("expiration_date", "유효기간"),
                new PresetField("score_report", "성적표")
        ));
        PRESET_MAP.put(ExperienceType.LICENSE, List.of(
                new PresetField("certificate_name", "자격증명"),
                new PresetField("organization", "발급기관"),
                new PresetField("acquisition_date", "취득일"),
                new PresetField("expiration_date", "유효기간"),
                new PresetField("certificate_copy", "자격증 사본")
        ));
        PRESET_MAP.put(ExperienceType.AWARD, List.of(
                new PresetField("award_name", "수상명"),
                new PresetField("organization", "수여기관"),
                new PresetField("award_date", "수상일"),
                new PresetField("award_grade", "수상 구분"),
                new PresetField("award_proof", "수상 증빙")
        ));
        PRESET_MAP.put(ExperienceType.COURSE, List.of(
                new PresetField("course_name", "과목명"),
                new PresetField("semester", "이수 학기"),
                new PresetField("credit", "학점"),
                new PresetField("grade", "성적"),
                new PresetField("major", "관련 분야")
        ));
        PRESET_MAP.put(ExperienceType.EDUCATION, List.of(
                new PresetField("education_name", "교육명"),
                new PresetField("organization", "운영기관"),
                new PresetField("period", "교육 기간"),
                new PresetField("completion_status", "수료 여부"),
                new PresetField("completion_certificate", "수료증")
        ));
    }

    public List<PresetField> getPresetFields(ExperienceType type) {
        return PRESET_MAP.getOrDefault(type, Collections.emptyList());
    }

    public ExperienceGroup getExperienceGroup(ExperienceType type) {
        return SPEC_TYPES.contains(type) ? ExperienceGroup.SPEC : ExperienceGroup.NARRATIVE;
    }

    public List<AiExperiencePresetSchema> getAiPresetSchemas(Collection<ExperienceType> types) {
        if (types == null || types.isEmpty()) {
            return List.of();
        }

        return new LinkedHashSet<>(types).stream()
                .map(type -> new AiExperiencePresetSchema(
                        toKoreanGroup(getExperienceGroup(type)),
                        type.name(),
                        type.getKoreanName(),
                        getPresetFields(type).stream()
                                .map(field -> new AiExperiencePresetSchema.Field(
                                        field.key(),
                                        field.label()
                                ))
                                .toList()
                ))
                .toList();
    }

    /**
     * AI가 자의적으로 바꾼 필드명(예: '역할분담', '배정부서')을 백엔드의 표준 키명으로 매핑해주는 보정 유틸리티
     */
    public String normalizeKey(ExperienceType type, String rawKey) {
        if (rawKey == null) return null;
        String cleanKey = clean(rawKey);

        for (PresetField field : getPresetFields(type)) {
            String cleanFieldKey = clean(field.key());
            String cleanFieldLabel = clean(field.label());
            if (cleanFieldKey.equals(cleanKey) || cleanFieldLabel.equals(cleanKey)) {
                return field.key();
            }
        }
        return rawKey;
    }

    public Map<String, Object> normalizeAttributes(
            ExperienceType type,
            Map<String, Object> attributes
    ) {
        if (attributes == null || attributes.isEmpty()) {
            return new LinkedHashMap<>();
        }

        Map<String, Object> normalizedAttributes = new LinkedHashMap<>();
        attributes.forEach((key, value) ->
                normalizedAttributes.put(normalizeKey(type, key), value)
        );
        return normalizedAttributes;
    }

    private String clean(String value) {
        return value.replace(" ", "")
                .replace("_", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String toKoreanGroup(ExperienceGroup group) {
        return group == ExperienceGroup.NARRATIVE ? "상세 서술형" : "스펙·증빙형";
    }
}
