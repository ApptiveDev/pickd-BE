package back.pickd.user.dto;

import back.pickd.user.entity.User;
import back.pickd.user.entity.UserEducation;
import back.pickd.user.entity.UserInterest;
import back.pickd.user.entity.UserLocation;
import back.pickd.user.entity.UserPrepStatus;
import back.pickd.user.entity.enums.DegreeType;
import back.pickd.user.entity.enums.EnrollmentStatus;
import back.pickd.user.entity.enums.OnboardingStep;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserProfileDto {

    private String email;
    private String name;
    private String nickname;
    private String picture;
    private String phone;
    private String birthDate;
    private String intro;
    private OnboardingStep onboardingStep;

    private String currentResidence;
    private List<String> desiredLocations;
    private String detailedAddress;

    private String schoolName;
    private String department;
    private String doubleMajor;
    private String minor;
    private DegreeType degreeType;
    private EnrollmentStatus enrollmentStatus;
    private String graduationDate;
    private Double gpa;
    private String campus;

    private List<String> industries;
    private List<String> jobGroups;
    private String employmentType;
    private List<String> companyTypes;
    private List<String> keywords;
    private String targetCompany;
    private String salaryRange;

    private String targetPeriod;
    private String currentStage;
    private List<String> focusItems;
    private Boolean hasResume;
    private Boolean hasBaseEssay;
    private Boolean hasPortfolio;

    public static UserProfileDto from(User user) {
        UserLocation location = user.getLocation();
        UserEducation education = user.getEducation();
        UserInterest interest = user.getInterest();
        UserPrepStatus prepStatus = user.getPrepStatus();

        return UserProfileDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .picture(user.getPicture())
                .phone(user.getPhone())
                .birthDate(user.getBirthDate())
                .intro(user.getIntro())
                .onboardingStep(user.getOnboardingStep())
                .currentResidence(location != null ? location.getCurrentResidence() : null)
                .desiredLocations(location != null ? location.getDesiredLocations() : null)
                .detailedAddress(location != null ? location.getDetailedAddress() : null)
                .schoolName(education != null ? education.getSchoolName() : null)
                .department(education != null ? education.getDepartment() : null)
                .doubleMajor(education != null ? education.getDoubleMajor() : null)
                .minor(education != null ? education.getMinor() : null)
                .degreeType(education != null ? education.getDegreeType() : null)
                .enrollmentStatus(education != null ? education.getEnrollmentStatus() : null)
                .graduationDate(education != null ? education.getGraduationDate() : null)
                .gpa(education != null ? education.getGpa() : null)
                .campus(education != null ? education.getCampus() : null)
                .industries(interest != null ? interest.getIndustries() : null)
                .jobGroups(interest != null ? interest.getJobGroups() : null)
                .employmentType(interest != null ? interest.getEmploymentType() : null)
                .companyTypes(interest != null ? interest.getCompanyTypes() : null)
                .keywords(interest != null ? interest.getKeywords() : null)
                .targetCompany(interest != null ? interest.getTargetCompany() : null)
                .salaryRange(interest != null ? interest.getSalaryRange() : null)
                .targetPeriod(prepStatus != null ? prepStatus.getTargetPeriod() : null)
                .currentStage(prepStatus != null ? prepStatus.getCurrentStage() : null)
                .focusItems(prepStatus != null ? prepStatus.getFocusItems() : null)
                .hasResume(prepStatus != null ? prepStatus.isHasResume() : null)
                .hasBaseEssay(prepStatus != null ? prepStatus.isHasBaseEssay() : null)
                .hasPortfolio(prepStatus != null ? prepStatus.isHasPortfolio() : null)
                .build();
    }
}
