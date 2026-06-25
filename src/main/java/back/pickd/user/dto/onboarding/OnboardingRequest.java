package back.pickd.user.dto.onboarding;

import back.pickd.user.entity.enums.DegreeType;
import back.pickd.user.entity.enums.EnrollmentStatus;
import back.pickd.user.entity.enums.OnboardingStep;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OnboardingRequest {
    // Step 1: Terms
    private Boolean serviceAgreed;
    private Boolean privacyAgreed;
    private Boolean marketingAgreed;
    private Boolean pushAgreed;

    // Step 1.5: Verification
    private String name;
    @Pattern(regexp = "^\\d{8}$", message = "생년월일 형식은 YYYYMMDD여야 합니다.")
    private String birthDate;
    private String phone;

    // Step 2: Basic Info
    private String nickname;
    private String intro;
    private String currentResidence;
    private List<String> desiredLocations;
    private String detailedAddress;

    // Step 3: Education
    private String schoolName;
    private String department;
    private String doubleMajor;
    private String minor;
    private DegreeType degreeType;
    private EnrollmentStatus enrollmentStatus;
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "졸업년월 형식은 YYYY-MM이어야 합니다.")
    private String graduationDate;
    private Double gpa;
    private String campus;

    // Step 4: Interests
    private List<String> industries;
    private List<String> jobGroups;
    private String employmentType;
    private List<String> companyTypes;
    private List<String> keywords;
    private String targetCompany;
    private String salaryRange;

    // Step 5: Prep Status
    private String targetPeriod;
    private String currentStage;
    private List<String> focusItems;
    private Boolean hasResume;
    private Boolean hasBaseEssay;
    private Boolean hasPortfolio;
    private List<ExperienceDto> experiences;
    private List<CertificationDto> certifications;

    /**
     * 요청에 포함된 필드를 기반으로 현재 온보딩 단계를 판별한다.
     * 각 단계를 나타내는 대표 필드가 null이 아니면 해당 단계로 판단한다.
     */
    public OnboardingStep detectStep() {
        if (targetPeriod != null)  return OnboardingStep.COMPLETED;
        if (industries != null)    return OnboardingStep.INTERESTS;
        if (schoolName != null)    return OnboardingStep.EDUCATION;
        if (nickname != null)      return OnboardingStep.BASIC;
        if (name != null)          return OnboardingStep.VERIFICATION;
        if (serviceAgreed != null) return OnboardingStep.TERMS;
        return null;
    }

    @Getter @NoArgsConstructor
    public static class ExperienceDto {
        private String type;
        private String title;
        private String startDate;
        private String endDate;
    }

    @Getter @NoArgsConstructor
    public static class CertificationDto {
        private String name;
        private String score;
        @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "취득년월 형식은 YYYY-MM이어야 합니다.")
        private String acquisitionDate;
    }
}
