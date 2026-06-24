package back.pickd.document.entity;

import back.pickd.application.entity.Application;
import back.pickd.document.enums.DocumentStatus;
import back.pickd.document.enums.DocumentType;
import back.pickd.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    private String title;
    private String company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DocumentStatus status;

    private Integer progress;

    @Column(length = 3000)
    private String content;

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

    public void update(String title, String company, DocumentType type,
                       DocumentStatus status, Integer progress, String content) {
        this.title = title;
        this.company = company;
        this.type = type;
        this.status = status;
        this.progress = progress;
        this.content = content;
    }
}
