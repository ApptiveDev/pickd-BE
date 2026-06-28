package back.pickd.experience.enums;

import java.util.Map;

public enum ExperienceType {
    PROJECT("프로젝트"),
    ACTIVITY("대외활동"),
    INTERN("인턴/직무경험"),
    CONTEST("공모전"),
    VOLUNTEER("봉사활동"),
    EXCHANGE("교환학생"),
    LANGUAGE("어학"),
    LICENSE("자격증"),
    AWARD("수상"),
    COURSE("수강과목"),
    EDUCATION("교육 이수"),
    ALBA("알바"),
    RESEARCH("학부연구생");

    private final String koreanName;

    ExperienceType(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public static ExperienceType fromKoreanName(String koreanName) {
        // 1. 정확히 일치
        for (ExperienceType type : values()) {
            if (type.getKoreanName().equals(koreanName)) {
                return type;
            }
        }
        // 2. 부분 일치 (예: "인턴" → "인턴/직무경험")
        for (ExperienceType type : values()) {
            if (type.getKoreanName().startsWith(koreanName) || koreanName.startsWith(type.getKoreanName())) {
                return type;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 경험 유형입니다: " + koreanName);
    }

    /**
     * 이 경험 유형의 기본 그룹을 반환한다.
     * 스펙·증빙 계열(AWARD, LICENSE, LANGUAGE, COURSE, EXCHANGE)은 SPEC,
     * 나머지는 NARRATIVE.
     */
    public ExperienceGroup defaultGroup() {
        return switch (this) {
            case AWARD, LICENSE, LANGUAGE, COURSE, EXCHANGE -> ExperienceGroup.SPEC;
            default -> ExperienceGroup.NARRATIVE;
        };
    }

    /**
     * 이 경험 유형에 맞는 attributes 맵을 생성한다.
     * AWARD는 award_date 키, 나머지는 period 키를 사용한다.
     */
    public Map<String, Object> buildAttributes(String startDate, String endDate) {
        if (this == AWARD) {
            return Map.of("award_date", endDate != null ? endDate : "");
        }
        String period = (startDate != null ? startDate : "") + " ~ " + (endDate != null ? endDate : "");
        return Map.of("period", period);
    }
}
