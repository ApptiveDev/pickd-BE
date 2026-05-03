package back.pickd.notice.entity;

import back.pickd.notice.entity.enums.JobCategory;
import back.pickd.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "notices")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "notice_name", nullable = false, length = 255)
    private String noticeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobCategory category;

    @Column(name = "employment_type", length = 50)
    private String employmentType;

    @Column(name = "posted_at", nullable = false)
    private LocalDateTime postedAt;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "notice_url", columnDefinition = "TEXT")
    private String noticeUrl;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer headcount;

    @Column(name = "region_1depth", length = 50)
    private String region1depth;

    @Column(name = "workplace_address", columnDefinition = "TEXT")
    private String workplaceAddress;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
    public Notice(User user, String companyName, String noticeName, JobCategory category,
                  String employmentType, LocalDateTime postedAt, LocalDateTime startedAt,
                  LocalDateTime endedAt, String noticeUrl, Integer headcount,
                  String region1depth, String workplaceAddress) {
        this.user = user;
        this.companyName = companyName;
        this.noticeName = noticeName;
        this.category = category;
        this.employmentType = employmentType;
        this.postedAt = postedAt;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.noticeUrl = noticeUrl;
        this.headcount = headcount != null ? headcount : 0;
        this.region1depth = region1depth;
        this.workplaceAddress = workplaceAddress;
    }
}
