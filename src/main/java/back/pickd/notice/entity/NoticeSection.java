package back.pickd.notice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notice_sections")
public class NoticeSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @Column(name = "section_name", nullable = false)
    private String sectionName;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(name = "responsibilities", columnDefinition = "TEXT")
    private String responsibilities;

    @Column(name = "headcount")
    private String headcount;

    @Column(name = "workplace")
    private String workplace;

    @Builder
    public NoticeSection(Notice notice, String sectionName, String jobTitle,
                         String responsibilities, String headcount, String workplace) {
        this.notice = notice;
        this.sectionName = sectionName;
        this.jobTitle = jobTitle;
        this.responsibilities = responsibilities;
        this.headcount = headcount;
        this.workplace = workplace;
    }

    public void assignToNotice(Notice notice) {
        this.notice = notice;
    }
}
