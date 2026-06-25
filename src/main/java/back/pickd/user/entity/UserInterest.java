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
@Table(name = "user_interests")
public class UserInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ElementCollection
    @CollectionTable(name = "user_interest_industries",
            joinColumns = @JoinColumn(name = "user_interest_id"),
            indexes = @Index(name = "idx_user_interest_industries", columnList = "user_interest_id"))
    @Column(name = "industry")
    private List<String> industries = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_interest_job_groups",
            joinColumns = @JoinColumn(name = "user_interest_id"),
            indexes = @Index(name = "idx_user_interest_job_groups", columnList = "user_interest_id"))
    @Column(name = "job_group")
    private List<String> jobGroups = new ArrayList<>();

    @Column(nullable = false)
    private String employmentType; // e.g., "FULL_TIME", "INTERN"

    @ElementCollection
    @CollectionTable(name = "user_interest_company_types",
            joinColumns = @JoinColumn(name = "user_interest_id"),
            indexes = @Index(name = "idx_user_interest_company_types", columnList = "user_interest_id"))
    @Column(name = "company_type")
    private List<String> companyTypes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_interest_keywords",
            joinColumns = @JoinColumn(name = "user_interest_id"),
            indexes = @Index(name = "idx_user_interest_keywords", columnList = "user_interest_id"))
    @Column(name = "keyword")
    private List<String> keywords = new ArrayList<>();

    @Column
    private String specificJob;

    @Column
    private String targetCompany;

    @Column
    private String salaryRange;

    @Column
    private String jobPriority;

    @Column
    private String industryPriority;

    @Column
    private String workType;

    @ElementCollection
    @CollectionTable(name = "user_interest_apply_types",
            joinColumns = @JoinColumn(name = "user_interest_id"),
            indexes = @Index(name = "idx_user_interest_apply_types", columnList = "user_interest_id"))
    @Column(name = "apply_type")
    private List<String> applyTypes = new ArrayList<>();

    public void update(List<String> industries, List<String> jobGroups, String employmentType,
                       List<String> companyTypes, List<String> keywords, String specificJob,
                       String targetCompany, String salaryRange, String jobPriority,
                       String industryPriority, String workType, List<String> applyTypes) {
        this.industries = industries;
        this.jobGroups = jobGroups;
        this.employmentType = employmentType;
        this.companyTypes = companyTypes;
        this.keywords = keywords;
        this.specificJob = specificJob;
        this.targetCompany = targetCompany;
        this.salaryRange = salaryRange;
        this.jobPriority = jobPriority;
        this.industryPriority = industryPriority;
        this.workType = workType;
        this.applyTypes = applyTypes;
    }

    @Builder
    public UserInterest(User user, List<String> industries, List<String> jobGroups, String employmentType,
                        List<String> companyTypes, List<String> keywords, String specificJob,
                        String targetCompany, String salaryRange, String jobPriority,
                        String industryPriority, String workType, List<String> applyTypes) {
        this.user = user;
        this.industries = industries;
        this.jobGroups = jobGroups;
        this.employmentType = employmentType;
        this.companyTypes = companyTypes;
        this.keywords = keywords;
        this.specificJob = specificJob;
        this.targetCompany = targetCompany;
        this.salaryRange = salaryRange;
        this.jobPriority = jobPriority;
        this.industryPriority = industryPriority;
        this.workType = workType;
        this.applyTypes = applyTypes;
    }
}
