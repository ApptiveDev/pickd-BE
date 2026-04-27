package back.pickd.user.dto.onboarding;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class Step5PrepStatusRequest {
    private String targetPeriod;
    private String currentStage;
    private List<String> focusItems;
    private boolean hasResume;
    private boolean hasBaseEssay;
    private boolean hasPortfolio;

    // 추가 상세 정보
    private List<String> preparingExams;
    private Integer targetApplyCount;
    private List<ExperienceDto> experiences;
    private List<CertificationDto> certifications;

    @Getter
    @NoArgsConstructor
    public static class ExperienceDto {
        private String type; // INTERN, PROJECT, ACTIVITY, AWARD
        private String title;
        private String description;
        private String startDate;
        private String endDate;
    }

    @Getter
    @NoArgsConstructor
    public static class CertificationDto {
        private String type; // LICENSE, LANGUAGE
        private String name;
        private String score;
        private String acquisitionDate;
    }
}
