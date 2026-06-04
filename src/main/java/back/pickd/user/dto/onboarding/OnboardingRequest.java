package back.pickd.user.dto.onboarding;

import back.pickd.user.entity.enums.DegreeType;
import back.pickd.user.entity.enums.EnrollmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "온보딩 단계별 입력 요청")
@Getter
@NoArgsConstructor
public class OnboardingRequest {
    // Step 1: Terms
    @Schema(description = "서비스 이용약관 동의 여부", example = "true")
    private Boolean serviceAgreed;

    @Schema(description = "개인정보 처리방침 동의 여부", example = "true")
    private Boolean privacyAgreed;

    @Schema(description = "마케팅 정보 수신 동의 여부", example = "false")
    private Boolean marketingAgreed;

    @Schema(description = "푸시 알림 수신 동의 여부", example = "true")
    private Boolean pushAgreed;

    // Step 1.5: Verification
    @Schema(description = "사용자 실명", example = "홍길동")
    private String name;

    @Schema(description = "생년월일", example = "2000-01-01")
    private String birthDate;

    @Schema(description = "휴대폰 번호", example = "010-1234-5678")
    private String phone;

    // Step 2: Basic Info
    @Schema(description = "사용자 닉네임", example = "길동")
    private String nickname;

    @Schema(description = "자기소개", example = "문제를 구조화해서 해결하는 백엔드 개발자입니다.")
    private String intro;

    @Schema(description = "현재 거주지", example = "서울특별시 강남구")
    private String currentResidence;

    @Schema(description = "희망 근무 지역 목록", example = "[\"서울\", \"경기\"]")
    private List<String> desiredLocations;

    @Schema(description = "상세 주소", example = "테헤란로 123")
    private String detailedAddress;

    // Step 3: Education
    @Schema(description = "학교명", example = "픽디대학교")
    private String schoolName;

    @Schema(description = "학과/전공", example = "컴퓨터공학과")
    private String department;

    @Schema(description = "복수전공", example = "경영학")
    private String doubleMajor;

    @Schema(description = "부전공", example = "통계학")
    private String minor;

    @Schema(description = "학위 유형", example = "BACHELOR")
    private DegreeType degreeType;

    @Schema(description = "재학 상태", example = "GRADUATED")
    private EnrollmentStatus enrollmentStatus;

    @Schema(description = "졸업 또는 졸업 예정年月", example = "2026-02")
    private String graduationDate;

    @Schema(description = "학점", example = "4.1")
    private Double gpa;

    @Schema(description = "캠퍼스", example = "서울캠퍼스")
    private String campus;

    // Step 4: Interests
    @Schema(description = "관심 산업군 목록", example = "[\"IT\", \"금융\"]")
    private List<String> industries;

    @Schema(description = "관심 직군 목록", example = "[\"백엔드\", \"플랫폼\"]")
    private List<String> jobGroups;

    @Schema(description = "희망 고용 형태", example = "FULL_TIME")
    private String employmentType;

    @Schema(description = "희망 기업 유형 목록", example = "[\"스타트업\", \"대기업\"]")
    private List<String> companyTypes;

    @Schema(description = "관심 키워드 목록", example = "[\"Spring\", \"AI\", \"B2B\"]")
    private List<String> keywords;

    @Schema(description = "목표 기업", example = "Pickd")
    private String targetCompany;

    @Schema(description = "희망 연봉 범위", example = "4000-5000")
    private String salaryRange;

    // Step 5: Prep Status
    @Schema(description = "목표 취업 준비 기간", example = "2026 상반기")
    private String targetPeriod;

    @Schema(description = "현재 준비 단계", example = "서류 준비")
    private String currentStage;

    @Schema(description = "집중 준비 항목 목록", example = "[\"이력서\", \"포트폴리오\", \"코딩테스트\"]")
    private List<String> focusItems;

    @Schema(description = "이력서 보유 여부", example = "true")
    private Boolean hasResume;

    @Schema(description = "기본 자기소개서 보유 여부", example = "false")
    private Boolean hasBaseEssay;

    @Schema(description = "포트폴리오 보유 여부", example = "true")
    private Boolean hasPortfolio;

    @Schema(description = "온보딩 중 입력한 주요 경험 목록")
    private List<ExperienceDto> experiences;

    @Schema(description = "온보딩 중 입력한 자격증 목록")
    private List<CertificationDto> certifications;

    @Schema(description = "온보딩 경험 요약")
    @Getter @NoArgsConstructor
    public static class ExperienceDto {
        @Schema(description = "경험 유형", example = "PROJECT")
        private String type;

        @Schema(description = "경험 제목", example = "교내 캡스톤 프로젝트")
        private String title;

        @Schema(description = "시작일", example = "2025-03")
        private String startDate;

        @Schema(description = "종료일", example = "2025-06")
        private String endDate;
    }

    @Schema(description = "온보딩 자격증 요약")
    @Getter @NoArgsConstructor
    public static class CertificationDto {
        @Schema(description = "자격증명", example = "정보처리기사")
        private String name;

        @Schema(description = "점수 또는 등급", example = "합격")
        private String score;

        @Schema(description = "취득일", example = "2025-09")
        private String acquisitionDate;
    }
}
