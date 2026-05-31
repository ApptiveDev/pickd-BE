package back.pickd.notice.company;

import back.pickd.notice.notice.Notice;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "notice_company_infos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeCompanyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본키

    // 공고와 1:1 연관관계 (연관관계의 주인)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Notice notice; // 연결된 공고 (FK)

    @Column(name = "company_introduction", columnDefinition = "TEXT")
    private String companyIntroduction; // 기업 소개

    @Column(columnDefinition = "TEXT")
    private String mission; // 미션

    @Column(columnDefinition = "TEXT")
    private String vision; // 비전

    @Column(name = "ideal_candidate", columnDefinition = "TEXT")
    private String idealCandidate; // 인재상

    @Column(name = "business_overview", columnDefinition = "TEXT")
    private String businessOverview; // 사업 개요

    @Column(name = "working_conditions", columnDefinition = "TEXT")
    private String workingConditions; // 근무 조건

    @Column(columnDefinition = "TEXT")
    private String compensation; // 급여 / 보상 체계

    @Column(columnDefinition = "TEXT")
    private String benefits; // 복리후생

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

    /** Notice 단방향에서 호출 시 반대편 FK 설정용 */
    public void setNotice(Notice notice) {
        this.notice = notice;
    }

    @Builder
    public NoticeCompanyInfo(Notice notice, String companyIntroduction, String mission,
                             String vision, String idealCandidate, String businessOverview,
                             String workingConditions, String compensation, String benefits) {
        this.notice = notice;
        this.companyIntroduction = companyIntroduction;
        this.mission = mission;
        this.vision = vision;
        this.idealCandidate = idealCandidate;
        this.businessOverview = businessOverview;
        this.workingConditions = workingConditions;
        this.compensation = compensation;
        this.benefits = benefits;
    }
}
