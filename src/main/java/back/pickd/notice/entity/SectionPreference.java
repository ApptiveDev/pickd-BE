package back.pickd.notice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "section_preferences")
public class SectionPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private NoticeSection section;

    @Column(name = "general_preference", columnDefinition = "TEXT")
    private String generalPreference;

    @Column(name = "additional_points", columnDefinition = "TEXT")
    private String additionalPoints;

    @Column(name = "veteran_preference", columnDefinition = "TEXT")
    private String veteranPreference;

    @Column(name = "disability_preference", columnDefinition = "TEXT")
    private String disabilityPreference;

    @Column(name = "certificate_preference", columnDefinition = "TEXT")
    private String certificatePreference;

    @Builder
    public SectionPreference(NoticeSection section, String generalPreference, String additionalPoints,
                             String veteranPreference, String disabilityPreference, String certificatePreference) {
        this.section = section;
        this.generalPreference = generalPreference;
        this.additionalPoints = additionalPoints;
        this.veteranPreference = veteranPreference;
        this.disabilityPreference = disabilityPreference;
        this.certificatePreference = certificatePreference;
    }
}
