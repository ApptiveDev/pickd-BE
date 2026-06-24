package back.pickd.notice.entity;

import back.pickd.coverletter.entity.CoverLetterItem;
import back.pickd.experience.entity.UserExperience;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ai_strategies")
public class AiStrategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_letter_item_id", nullable = false)
    private CoverLetterItem coverLetterItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experience_id")
    private UserExperience matchedExperience;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SwotStrategy strategy;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String jdTargeting;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String dynamicFraming;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String strategyDerivation;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String writingGuide;

    public enum SwotStrategy {
        SO, ST, WO, WT, NA
    }

    @Builder
    public AiStrategy(CoverLetterItem coverLetterItem, UserExperience matchedExperience,
                      SwotStrategy strategy, String jdTargeting,
                      String dynamicFraming, String strategyDerivation, String writingGuide) {
        this.coverLetterItem = coverLetterItem;
        this.matchedExperience = matchedExperience;
        this.strategy = strategy;
        this.jdTargeting = jdTargeting;
        this.dynamicFraming = dynamicFraming;
        this.strategyDerivation = strategyDerivation;
        this.writingGuide = writingGuide;
    }
}
