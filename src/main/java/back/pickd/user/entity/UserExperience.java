package back.pickd.user.entity;

import back.pickd.user.entity.enums.ExperienceGroup;
import back.pickd.user.entity.enums.ExperienceType;
import back.pickd.user.entity.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "user_experiences")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserExperience {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_type", nullable = false, length = 50)
    private ExperienceType experienceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_group", nullable = false, length = 20)
    private ExperienceGroup experienceGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "document_content", columnDefinition = "TEXT")
    private String documentContent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", nullable = false)
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "keywords", nullable = false)
    @Builder.Default
    private List<String> keywords = new ArrayList<>();

    @OneToMany(mappedBy = "userExperience", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExperienceLink> links = new ArrayList<>();

    @OneToMany(mappedBy = "userExperience", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExperienceFile> files = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateLinks(List<ExperienceLink> newLinks) {
        this.links.clear();
        if (newLinks != null) {
            newLinks.forEach(link -> {
                link.setUserExperience(this);
                this.links.add(link);
            });
        }
    }

    public void updateFiles(List<ExperienceFile> newFiles) {
        this.files.clear();
        if (newFiles != null) {
            newFiles.forEach(file -> {
                file.setUserExperience(this);
                this.files.add(file);
            });
        }
    }
}
