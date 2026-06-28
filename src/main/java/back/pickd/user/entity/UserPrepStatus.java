package back.pickd.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_prep_statuses")
public class UserPrepStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String targetPeriod;

    @Column(nullable = false)
    private String currentStage;

    @ElementCollection
    @CollectionTable(name = "user_focus_items",
            joinColumns = @JoinColumn(name = "user_prep_status_id"),
            indexes = @Index(name = "idx_user_focus_items", columnList = "user_prep_status_id"))
    @Column(name = "focus_item")
    private List<String> focusItems = new ArrayList<>();

    @Column
    private boolean hasResume;

    @Column
    private boolean hasBaseEssay;

    @Column
    private boolean hasPortfolio;

    @ElementCollection
    @CollectionTable(name = "user_preparing_exams",
            joinColumns = @JoinColumn(name = "user_prep_status_id"),
            indexes = @Index(name = "idx_user_preparing_exams", columnList = "user_prep_status_id"))
    @Column(name = "exam_name")
    private List<String> preparingExams = new ArrayList<>();

    @Column
    private Integer targetApplyCount;

    public void update(String targetPeriod, String currentStage, List<String> focusItems,
                       boolean hasResume, boolean hasBaseEssay, boolean hasPortfolio,
                       List<String> preparingExams, Integer targetApplyCount) {
        this.targetPeriod = targetPeriod;
        this.currentStage = currentStage;
        this.focusItems = focusItems;
        this.hasResume = hasResume;
        this.hasBaseEssay = hasBaseEssay;
        this.hasPortfolio = hasPortfolio;
        this.preparingExams = preparingExams;
        this.targetApplyCount = targetApplyCount;
    }

    @Builder
    public UserPrepStatus(User user, String targetPeriod, String currentStage, List<String> focusItems,
                          boolean hasResume, boolean hasBaseEssay, boolean hasPortfolio,
                          List<String> preparingExams, Integer targetApplyCount) {
        this.user = user;
        this.targetPeriod = targetPeriod;
        this.currentStage = currentStage;
        this.focusItems = focusItems;
        this.hasResume = hasResume;
        this.hasBaseEssay = hasBaseEssay;
        this.hasPortfolio = hasPortfolio;
        this.preparingExams = preparingExams;
        this.targetApplyCount = targetApplyCount;
    }
}
