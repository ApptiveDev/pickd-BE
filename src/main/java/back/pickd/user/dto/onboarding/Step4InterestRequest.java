package back.pickd.user.dto.onboarding;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class Step4InterestRequest {
    private List<String> industries;
    private List<String> jobGroups;
    private String employmentType;
    private List<String> companyTypes;
    private List<String> keywords;
    private String specificJob;
    private String targetCompany;
    private String salaryRange;
    private String jobPriority;
    private String industryPriority;
    private String workType;
    private List<String> applyTypes;
}
