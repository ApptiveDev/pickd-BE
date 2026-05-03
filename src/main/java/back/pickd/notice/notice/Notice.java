package back.pickd.notice.notice;

import back.pickd.notice.enums.JobCategory;
import back.pickd.notice.section.NoticeSection;
import back.pickd.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    // ===================== 연관관계 (양방향 - 중간 노드만) =====================

    /**
     * NoticeSection은 자식(자격요건, 우대사항, 문항)을 가진 중간 노드이므로 cascade 유지
     * NoticeProcess, ApplicationDocument, NoticeCompanyInfo, NoticeGuideline은
     * 리프 노드이므로 단방향(@ManyToOne)으로만 연결하고 별도 Repository로 저장
     */
    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticeSection> sections = new ArrayList<>(); // 모집 부문 목록

    // ===================== 연관관계 편의 메소드 =====================

    /** 모집 부문 추가 (양방향 동기화) */
    public void addSection(NoticeSection section) {
        sections.add(section);
        section.setNotice(this); // 반대편 FK 설정
    }

    // ===================== 생명주기 =====================

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

    // ===================== 생성자 =====================

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
