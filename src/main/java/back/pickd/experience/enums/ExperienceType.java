package back.pickd.experience.enums;

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
        for (ExperienceType type : values()) {
            if (type.getKoreanName().equals(koreanName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 경험 유형입니다: " + koreanName);
    }
}
