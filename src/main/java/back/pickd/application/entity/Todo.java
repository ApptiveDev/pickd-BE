package back.pickd.application.entity;

import back.pickd.user.entity.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "todos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    private String title;

    @Builder.Default
    private boolean completed = false;

    private LocalDateTime dueDateTime;

    @Column(length = 1000)
    private String memo;

    @Column(name = "calendar_event_id")
    private String calendarEventId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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

    public void toggleCompleted() {
        this.completed = !this.completed;
    }

    public void update(String title, LocalDateTime dueDateTime, String memo) {
        if (title      != null) this.title       = title;
        if (dueDateTime != null) this.dueDateTime = dueDateTime;
        if (memo       != null) this.memo        = memo;
    }

    public void assignCalendarEventId(String eventId) {
        this.calendarEventId = eventId;
    }
}
