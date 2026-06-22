package back.pickd.experience.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "experience_duplicate_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExperienceDuplicateGroup {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private ExperienceExtractionBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "existing_experience_id", nullable = false)
    private UserExperience existingExperience;

    @OneToMany(mappedBy = "duplicateGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExperienceExtractionDraft> drafts = new ArrayList<>();

    public ExperienceDuplicateGroup(UserExperience existingExperience) {
        this.id = UUID.randomUUID().toString();
        this.existingExperience = existingExperience;
    }

    void attachTo(ExperienceExtractionBatch batch) {
        this.batch = batch;
    }

    public void addDraft(ExperienceExtractionDraft draft) {
        draft.attachTo(this);
        drafts.add(draft);
    }
}
