package back.pickd.notice.qualification;

import back.pickd.notice.section.NoticeSection;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "section_qualifications")
public class SectionQualification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private NoticeSection section; // 부모 모집 부문 (FK)

    // 포괄적 자격 설명
    @Column(name = "general_qualification", columnDefinition = "TEXT")
    private String generalQualification; // 지원 자격 (일반)

    @Column(name = "mandatory_qualification", columnDefinition = "TEXT")
    private String mandatoryQualification; // 필수 자격

    @Column(name = "eligibility_requirement", columnDefinition = "TEXT")
    private String eligibilityRequirement; // 응시 자격

    // 세부 요건
    @Column(name = "education_requirement", length = 255)
    private String educationRequirement; // 학력 요건 (예: 대졸(4년) 이상)

    @Column(name = "major_requirement", length = 255)
    private String majorRequirement; // 전공 요건 (예: 컴퓨터공학 및 관련 학과)

    @Column(name = "certificate_requirement", columnDefinition = "TEXT")
    private String certificateRequirement; // 자격증 요건 (예: 정보처리기사 필수)

    @Column(name = "language_requirement", length = 255)
    private String languageRequirement; // 어학 요건 (예: TOEIC 800 이상)

    @Column(name = "experience_requirement", length = 255)
    private String experienceRequirement; // 경력 요건 (예: 신입, 경력 3년 이상)

    // 인적 요건
    @Column(name = "age_requirement", length = 100)
    private String ageRequirement; // 연령 요건 (예: 만 34세 이하)

    @Column(name = "military_requirement", length = 100)
    private String militaryRequirement; // 병역 요건 (예: 군필 또는 면제자)

    @Column(name = "other_requirements", columnDefinition = "TEXT")
    private String otherRequirements; // 기타 필수 조건

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 생성 일시

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정 일시

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public SectionQualification(NoticeSection section, String generalQualification,
                                String mandatoryQualification, String eligibilityRequirement,
                                String educationRequirement, String majorRequirement,
                                String certificateRequirement, String languageRequirement,
                                String experienceRequirement, String ageRequirement,
                                String militaryRequirement, String otherRequirements) {
        this.section = section;
        this.generalQualification = generalQualification;
        this.mandatoryQualification = mandatoryQualification;
        this.eligibilityRequirement = eligibilityRequirement;
        this.educationRequirement = educationRequirement;
        this.majorRequirement = majorRequirement;
        this.certificateRequirement = certificateRequirement;
        this.languageRequirement = languageRequirement;
        this.experienceRequirement = experienceRequirement;
        this.ageRequirement = ageRequirement;
        this.militaryRequirement = militaryRequirement;
        this.otherRequirements = otherRequirements;
    }
}
