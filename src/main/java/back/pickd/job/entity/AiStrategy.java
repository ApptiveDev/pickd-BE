package back.pickd.job.entity;

import back.pickd.user.entity.User;
import back.pickd.user.entity.UserExperience;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "ai_strategies")
public class AiStrategy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private JobAnnouncement jobAnnouncement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experience_id")
    private UserExperience matchedExperience;

    @Column(columnDefinition = "TEXT")
    private String essayQuestion;

    @Enumerated(EnumType.STRING)
    private SwotStrategy strategy;

    @Column(columnDefinition = "TEXT")
    private String jdTargeting;

    @Column(columnDefinition = "TEXT")
    private String dynamicFraming;

    @Column(columnDefinition = "TEXT")
    private String strategyDerivation;

    @Column(columnDefinition = "TEXT")
    private String writingGuide;

    public enum SwotStrategy {
        SO, ST, WO, WT, NA
    }

    @Builder
    public AiStrategy(User user, JobAnnouncement jobAnnouncement, UserExperience matchedExperience,
                      String essayQuestion, SwotStrategy strategy, String jdTargeting,
                      String dynamicFraming, String strategyDerivation, String writingGuide) {
        this.user = user;
        this.jobAnnouncement = jobAnnouncement;
        this.matchedExperience = matchedExperience;
        this.essayQuestion = essayQuestion;
        this.strategy = strategy;
        this.jdTargeting = jdTargeting;
        this.dynamicFraming = dynamicFraming;
        this.strategyDerivation = strategyDerivation;
        this.writingGuide = writingGuide;
    }
}
