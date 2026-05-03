package back.pickd.notice.guideline;

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
@Table(name = "notice_guidelines")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeGuideline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본키

    // 공고와 1:1 연관관계 (연관관계의 주인)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Notice notice; // 연결된 공고 (FK)

    @Column(name = "general_notes", columnDefinition = "TEXT")
    private String generalNotes; // 일반 유의사항

    @Column(name = "duplicate_apply_restriction", columnDefinition = "TEXT")
    private String duplicateApplyRestriction; // 중복 지원 제한

    @Column(name = "false_info_warning", columnDefinition = "TEXT")
    private String falseInfoWarning; // 허위 기재 경고

    @Column(name = "cancellation_conditions", columnDefinition = "TEXT")
    private String cancellationConditions; // 합격 취소 조건

    @Column(name = "recruitment_cancel_possibility", columnDefinition = "TEXT")
    private String recruitmentCancelPossibility; // 채용 취소 가능성 안내

    @Column(name = "reserve_candidate_guide", columnDefinition = "TEXT")
    private String reserveCandidateGuide; // 예비 합격자 안내

    @Column(name = "contact_info", columnDefinition = "TEXT")
    private String contactInfo; // 문의처

    @Column(name = "other_guides", columnDefinition = "TEXT")
    private String otherGuides; // 기타 안내사항

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
    public NoticeGuideline(Notice notice, String generalNotes, String duplicateApplyRestriction,
                           String falseInfoWarning, String cancellationConditions,
                           String recruitmentCancelPossibility, String reserveCandidateGuide,
                           String contactInfo, String otherGuides) {
        this.notice = notice;
        this.generalNotes = generalNotes;
        this.duplicateApplyRestriction = duplicateApplyRestriction;
        this.falseInfoWarning = falseInfoWarning;
        this.cancellationConditions = cancellationConditions;
        this.recruitmentCancelPossibility = recruitmentCancelPossibility;
        this.reserveCandidateGuide = reserveCandidateGuide;
        this.contactInfo = contactInfo;
        this.otherGuides = otherGuides;
    }
}
