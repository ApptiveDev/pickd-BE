package back.pickd.notice.strategy;

import back.pickd.notice.question.ApplicationQuestion;
import back.pickd.user.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private ApplicationQuestion applicationQuestion;

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
    public AiStrategy(User user, ApplicationQuestion applicationQuestion, UserExperience matchedExperience,
                      SwotStrategy strategy, String jdTargeting,
                      String dynamicFraming, String strategyDerivation, String writingGuide) {
        this.user = user;
        this.applicationQuestion = applicationQuestion;
        this.matchedExperience = matchedExperience;
        this.strategy = strategy;
        this.jdTargeting = jdTargeting;
        this.dynamicFraming = dynamicFraming;
        this.strategyDerivation = strategyDerivation;
        this.writingGuide = writingGuide;
    }
}
