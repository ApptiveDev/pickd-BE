package back.pickd.coverletter.entity;

import back.pickd.application.entity.Application;
import back.pickd.notice.entity.Notice;
import back.pickd.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cover_letter_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CoverLetterItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    @Column(name = "ai_generated", nullable = false)
    @Builder.Default
    private boolean aiGenerated = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.notice == null && this.application == null) {
            throw new IllegalStateException("CoverLetterItem은 Notice 또는 Application 중 하나에 반드시 연결되어야 합니다.");
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── 도메인 메서드 ──────────────────────────────────────────────────────────

    public void update(String question, String answer, Integer maxLength, Integer orderIndex) {
        this.question = question;
        this.answer = answer;
        this.maxLength = maxLength;
        this.orderIndex = orderIndex;
    }

    public void writeAnswer(String answer) {
        this.answer = answer;
    }
}
