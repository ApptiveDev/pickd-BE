package back.pickd.experience.entity;

import back.pickd.experience.enums.ExtractionBatchStatus;
import back.pickd.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "experience_extraction_batches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExperienceExtractionBatch {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExtractionBatchStatus status;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExperienceDuplicateGroup> groups = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    public ExperienceExtractionBatch(User user) {
        this.id = UUID.randomUUID().toString();
        this.user = user;
        this.status = ExtractionBatchStatus.PENDING;
        this.createdAt = OffsetDateTime.now();
    }

    public void addGroup(ExperienceDuplicateGroup group) {
        group.attachTo(this);
        groups.add(group);
    }

    public void complete() {
        this.status = ExtractionBatchStatus.COMPLETED;
        this.processedAt = OffsetDateTime.now();
        this.groups.clear();
    }
}
