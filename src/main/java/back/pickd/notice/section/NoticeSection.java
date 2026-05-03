package back.pickd.notice.section;

import back.pickd.notice.notice.Notice;
import back.pickd.notice.preference.SectionPreference;
import back.pickd.notice.qualification.SectionQualification;
import back.pickd.notice.question.ApplicationQuestion;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "notice_sections")
public class NoticeSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // DB 단의 ON DELETE CASCADE 적용
    private Notice notice; // 부모 공고 (FK)

    @Column(name = "section_name", nullable = false, length = 100)
    private String sectionName; // 모집 부문명 (예: IT 본부, 공통 부문)

    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle; // 직무명 (예: 백엔드 개발자)

    @Column(name = "sub_job_title", length = 100)
    private String subJobTitle; // 세부 직무명 (예: Java/Spring)

    @Column(columnDefinition = "TEXT")
    private String responsibilities; // 담당 업무 (핵심 요약)

    @Column(name = "detailed_description", columnDefinition = "TEXT")
    private String detailedDescription; // 세부 업무 설명 (상세 리스트 등)

    @Column(length = 255)
    private String workplace; // 부문별 근무지 (특정 사무소명 등)

    @Column(length = 50)
    private String headcount; // 부문별 채용 인원 (숫자가 아닐 수 있어 VARCHAR)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 생성 일시

    // ===================== 연관관계 (양방향) =====================

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SectionQualification> qualifications = new ArrayList<>(); // 지원 자격 목록

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SectionPreference> preferences = new ArrayList<>(); // 우대사항 목록

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationQuestion> questions = new ArrayList<>(); // 자소서 문항 목록

    // ===================== 연관관계 편의 메소드 =====================

    /** 지원 자격 추가 (양방향 동기화) */
    public void addQualification(SectionQualification qualification) {
        qualifications.add(qualification);
        qualification.setSection(this); // 반대편 FK 설정
    }

    /** 우대사항 추가 (양방향 동기화) */
    public void addPreference(SectionPreference preference) {
        preferences.add(preference);
        preference.setSection(this); // 반대편 FK 설정
    }

    /** 자소서 문항 추가 (양방향 동기화) */
    public void addQuestion(ApplicationQuestion question) {
        questions.add(question);
        question.setSection(this); // 반대편 FK 설정
    }

    /** Notice 단방향에서 호출 시 반대편 FK 설정용 */
    public void setNotice(Notice notice) {
        this.notice = notice;
    }

    // ===================== 생명주기 =====================

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // ===================== 생성자 =====================

    @Builder
    public NoticeSection(Notice notice, String sectionName, String jobTitle, String subJobTitle,
                         String responsibilities, String detailedDescription, String workplace,
                         String headcount) {
        this.notice = notice;
        this.sectionName = sectionName;
        this.jobTitle = jobTitle;
        this.subJobTitle = subJobTitle;
        this.responsibilities = responsibilities;
        this.detailedDescription = detailedDescription;
        this.workplace = workplace;
        this.headcount = headcount;
    }
}
