package back.pickd.application.entity;

import back.pickd.application.enums.ApplicationStatus;
import back.pickd.coverletter.entity.CoverLetterItem;
import back.pickd.document.entity.Document;
import back.pickd.notice.entity.Notice;
import back.pickd.user.entity.User;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "applications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    private String company;
    private String jobTitle;
    private String position;
    private String industry;
    private String memo;

    private LocalDateTime applyDate;
    private LocalDateTime interviewDate;
    private LocalDateTime deadlineDate;

    private String applyEventId;
    private String interviewEventId;
    private String deadlineEventId;

    @Column(nullable = false)
    @Builder.Default
    private boolean important = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @JsonManagedReference
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Todo> todos = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Document> documents = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CoverLetterItem> coverLetterItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── 도메인 메서드 ──────────────────────────────────────────────────────────

    public void update(String company, String jobTitle, String position, String industry,
                       ApplicationStatus status, boolean important, String memo,
                       LocalDateTime applyDate, LocalDateTime interviewDate, LocalDateTime deadlineDate) {
        this.company = company;
        this.jobTitle = jobTitle;
        this.position = position;
        this.industry = industry;
        this.status = status;
        this.important = important;
        this.memo = memo;
        this.applyDate = applyDate;
        this.interviewDate = interviewDate;
        this.deadlineDate = deadlineDate;
    }

    public void assignApplyEventId(String eventId) {
        this.applyEventId = eventId;
    }

    public void assignInterviewEventId(String eventId) {
        this.interviewEventId = eventId;
    }

    public void assignDeadlineEventId(String eventId) {
        this.deadlineEventId = eventId;
    }

    public void clearApplyEventId() {
        this.applyEventId = null;
    }

    public void clearInterviewEventId() {
        this.interviewEventId = null;
    }

    public void clearDeadlineEventId() {
        this.deadlineEventId = null;
    }

    public void assignNotice(Notice notice) {
        this.notice = notice;
    }
}
