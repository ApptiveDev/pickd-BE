package back.pickd.experience.entity;

import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.experience.enums.Status;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "experience_extraction_drafts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExperienceExtractionDraft {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duplicate_group_id", nullable = false)
    private ExperienceDuplicateGroup duplicateGroup;

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
    @Column(nullable = false)
    private Map<String, Object> attributes = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private List<String> keywords = new ArrayList<>();

    @Column(name = "resume_url", nullable = false, columnDefinition = "TEXT")
    private String resumeUrl;

    @Column(name = "similarity")
    private Double similarity;

    @Builder
    public ExperienceExtractionDraft(
            String title,
            ExperienceType experienceType,
            ExperienceGroup experienceGroup,
            Status status,
            String documentContent,
            Map<String, Object> attributes,
            List<String> keywords,
            String resumeUrl,
            Double similarity
    ) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.experienceType = experienceType;
        this.experienceGroup = experienceGroup;
        this.status = status;
        this.documentContent = documentContent;
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
        this.keywords = keywords != null ? new ArrayList<>(keywords) : new ArrayList<>();
        this.resumeUrl = resumeUrl;
        this.similarity = similarity;
    }

    void attachTo(ExperienceDuplicateGroup duplicateGroup) {
        this.duplicateGroup = duplicateGroup;
    }
}
