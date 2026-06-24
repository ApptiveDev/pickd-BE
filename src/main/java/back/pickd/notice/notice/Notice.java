package back.pickd.notice.notice;

import back.pickd.notice.enums.EmploymentType;
import back.pickd.notice.enums.JobCategory;
import back.pickd.notice.section.NoticeSection;
import back.pickd.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notices")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "notice_name", nullable = false)
    private String noticeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private JobCategory category;

    @Column(name = "started_at", nullable = false)
    private String startedAt;

    @Column(name = "ended_at")
    private String endedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type")
    private EmploymentType employmentType;

    @Column(name = "headcount")
    private String headcount;

    @Column(name = "region_1depth")
    private String region1depth;

    @Column(name = "workplace_address", columnDefinition = "TEXT")
    private String workplaceAddress;

    @Column(name = "notice_url", columnDefinition = "TEXT")
    private String noticeUrl;

    @Column(columnDefinition = "TEXT")
    private String opportunities;

    @Column(columnDefinition = "TEXT")
    private String threats;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticeSection> sections = new ArrayList<>();

    @Builder
    public Notice(User user, String companyName, String noticeName, JobCategory category,
                  String startedAt, String endedAt, EmploymentType employmentType, String headcount,
                  String region1depth, String workplaceAddress, String noticeUrl,
                  String opportunities, String threats) {
        this.user = user;
        this.companyName = companyName;
        this.noticeName = noticeName;
        this.category = category;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.employmentType = employmentType;
        this.headcount = headcount;
        this.region1depth = region1depth;
        this.workplaceAddress = workplaceAddress;
        this.noticeUrl = noticeUrl;
        this.opportunities = opportunities;
        this.threats = threats;
    }

    public void addSection(NoticeSection section) {
        sections.add(section);
        section.assignToNotice(this);
    }
}
