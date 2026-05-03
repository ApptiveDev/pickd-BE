package back.pickd.notice.notice;

import back.pickd.notice.enums.JobCategory;
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
    private Long id; // 기본키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 연결된 사용자 (N:1)

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName; // 기업명

    @Column(name = "notice_name", nullable = false, length = 255)
    private String noticeName; // 공고명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobCategory category; // 채용 구분 (ENUM)

    @Column(name = "employment_type", length = 50)
    private String employmentType; // 고용 형태 (상세 기술용)

    @Column(name = "posted_at", nullable = false)
    private LocalDateTime postedAt; // 공고 게시일

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt; // 접수 시작일

    @Column(name = "ended_at")
    private LocalDateTime endedAt; // 접수 마감일 (상시 채용일 경우 NULL 가능)

    @Column(name = "notice_url", columnDefinition = "TEXT")
    private String noticeUrl; // 지원 링크

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Integer headcount; // 채용 인원 (0은 '0명' 또는 '미정' 의미)

    @Column(name = "region_1depth", length = 50)
    private String region1depth; // 근무 지역 (예: 서울, 경기, 부산)

    @Column(name = "workplace_address", columnDefinition = "TEXT")
    private String workplaceAddress; // 상세 근무지 (예: 서울 강남구 테헤란로 123)

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
