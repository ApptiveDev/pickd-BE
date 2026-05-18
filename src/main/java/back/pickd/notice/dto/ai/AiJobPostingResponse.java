package back.pickd.notice.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiJobPostingResponse {
    private Long id;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("notice_name")
    private String noticeName;

    private String category;

    @JsonProperty("employment_type")
    private String employmentType;

    @JsonProperty("started_at")
    private String startedAt;

    @JsonProperty("ended_at")
    private String endedAt;

    @JsonProperty("notice_url")
    private String noticeUrl;

    private Integer headcount;

    @JsonProperty("region_1depth")
    private String region1depth;

    @JsonProperty("workplace_address")
    private String workplaceAddress;

    private List<AiNoticeSectionDto> sections;
    private List<AiNoticeProcessDto> processes;
    private List<AiApplicationDocumentDto> documents;
    private List<AiCitationDto> citations;

    @Builder
    public AiJobPostingResponse(Long id, String companyName, String noticeName, String category,
                                String employmentType, String startedAt, String endedAt, String noticeUrl,
                                Integer headcount, String region1depth, String workplaceAddress,
                                List<AiNoticeSectionDto> sections, List<AiNoticeProcessDto> processes,
                                List<AiApplicationDocumentDto> documents, List<AiCitationDto> citations) {
        this.id = id;
        this.companyName = companyName;
        this.noticeName = noticeName;
        this.category = category;
        this.employmentType = employmentType;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.noticeUrl = noticeUrl;
        this.headcount = headcount;
        this.region1depth = region1depth;
        this.workplaceAddress = workplaceAddress;
        this.sections = sections;
        this.processes = processes;
        this.documents = documents;
        this.citations = citations;
    }
}
