package back.pickd.experience.support;

import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Experience 도메인에서 반복 사용되는 Enum ↔ 한국어 변환 유틸리티.
 * ExperienceExtractionService, ExperienceExtractionV2Service, ExperienceMergeService에서 중복 제거.
 */
@Component
@RequiredArgsConstructor
public class ExperienceConversionUtils {

    private final PresetRegistry presetRegistry;

    public ExperienceGroup convertGroup(String koreanGroup) {
        return switch (koreanGroup) {
            case "상세 서술형" -> ExperienceGroup.NARRATIVE;
            case "스펙·증빙형" -> ExperienceGroup.SPEC;
            default -> throw new IllegalArgumentException("지원하지 않는 경험 그룹입니다: " + koreanGroup);
        };
    }

    public ExperienceType convertType(String koreanType) {
        return ExperienceType.fromKoreanName(koreanType);
    }

    public String toKoreanGroup(ExperienceGroup group) {
        if (group == null) return null;
        return group == ExperienceGroup.NARRATIVE ? "상세 서술형" : "스펙·증빙형";
    }

    public String toKoreanType(ExperienceType type) {
        return type != null ? type.getKoreanName() : null;
    }

    public void validateGroupType(ExperienceGroup group, ExperienceType type) {
        if (presetRegistry.getExperienceGroup(type) != group) {
            throw new IllegalArgumentException("경험 그룹과 유형이 일치하지 않습니다: " + type.getKoreanName());
        }
    }
}
