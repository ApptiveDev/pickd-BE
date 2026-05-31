package back.pickd.notice.dto;

import back.pickd.notice.enums.EmploymentType;
import back.pickd.notice.enums.JobCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeSaveRequestDto {

    @NotBlank(message = "기업명은 필수입니다.")
    private String companyName;

    @NotBlank(message = "공고명은 필수입니다.")
    private String noticeName;

    @NotNull(message = "채용 구분은 필수입니다.")
    private JobCategory category;

    @NotBlank(message = "접수 시작일은 필수입니다.")
    private String startedAt;

    private String endedAt;
    private EmploymentType employmentType;
    private String headcount;
    private String region1depth;
    private String workplaceAddress;
    private String noticeUrl;
    private String opportunities;
    private String threats;

    @Builder
    public NoticeSaveRequestDto(String companyName, String noticeName, JobCategory category, String startedAt,
                                String endedAt, EmploymentType employmentType, String headcount, String region1depth,
                                String workplaceAddress, String noticeUrl, String opportunities, String threats) {
        this.companyName = companyName;
        this.noticeName = noticeName;
        this.category = category;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.employmentType = employmentType;
        this.headcount = headcount;
        this.region1depth = region1depth;
        this.workplaceAddress = workplaceAddress;
        this.noticeUrl = noticeUrl;
        this.opportunities = opportunities;
        this.threats = threats;
    }
}
