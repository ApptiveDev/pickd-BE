package back.pickd.notice.section;

import back.pickd.notice.notice.Notice;
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

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

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
