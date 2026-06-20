package back.pickd.user.dto;

import back.pickd.user.entity.User;
import back.pickd.user.entity.UserEducation;
import back.pickd.user.entity.UserInterest;
import back.pickd.user.entity.UserLocation;
import back.pickd.user.entity.UserPrepStatus;
import back.pickd.user.entity.enums.DegreeType;
import back.pickd.user.entity.enums.EnrollmentStatus;
import back.pickd.user.entity.enums.OnboardingStep;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserProfileDtoTest {

    @Test
    void fromMapsOnboardingProfileFields() {
        User user = User.builder()
                .email("user@example.com")
                .name("기존 이름")
                .nickname("픽디")
                .picture("https://example.com/profile.png")
                .onboardingStep(OnboardingStep.COMPLETED)
                .build();
        user.verify("홍길동", "20010315", "010-0000-0000");
        user.updateIntro("백엔드 개발자를 준비하고 있습니다.");
        user.setLocation(UserLocation.builder()
                .user(user)
                .currentResidence("부산광역시")
                .desiredLocations(List.of("서울", "부산"))
                .detailedAddress("센텀로 17")
                .build());
        user.setEducation(UserEducation.builder()
                .user(user)
                .schoolName("부산대학교")
                .department("경영학과")
                .degreeType(DegreeType.BACHELOR)
                .enrollmentStatus(EnrollmentStatus.ENROLLED)
                .graduationDate("2026-02")
                .gpa(4.1)
                .campus("부산")
                .build());
        user.setInterest(UserInterest.builder()
                .user(user)
                .industries(List.of("IT"))
                .jobGroups(List.of("백엔드"))
                .employmentType("FULL_TIME")
                .companyTypes(List.of("스타트업"))
                .keywords(List.of("Spring"))
                .targetCompany("카카오")
                .salaryRange("3000~4000")
                .build());
        user.setPrepStatus(UserPrepStatus.builder()
                .user(user)
                .targetPeriod("2026 상반기")
                .currentStage("서류 준비")
                .focusItems(List.of("이력서", "자소서"))
                .hasResume(true)
                .hasBaseEssay(false)
                .hasPortfolio(true)
                .build());

        UserProfileDto response = UserProfileDto.from(user);

        assertEquals("user@example.com", response.getEmail());
        assertEquals("홍길동", response.getName());
        assertEquals("010-0000-0000", response.getPhone());
        assertEquals("부산광역시", response.getCurrentResidence());
        assertEquals(List.of("서울", "부산"), response.getDesiredLocations());
        assertEquals("부산대학교", response.getSchoolName());
        assertEquals("경영학과", response.getDepartment());
        assertEquals(List.of("백엔드"), response.getJobGroups());
        assertEquals("2026 상반기", response.getTargetPeriod());
        assertTrue(response.getHasResume());
        assertTrue(response.getHasPortfolio());
    }
}
