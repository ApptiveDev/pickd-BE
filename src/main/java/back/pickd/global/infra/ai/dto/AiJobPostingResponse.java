package back.pickd.global.infra.ai.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AiJobPostingResponse {
    private String companyName;
    private String noticeName;
    private String category;
    private String employmentType;
    private String startedAt;
    private String endedAt;
    private String noticeUrl;
    private String headcount;
    private String region1depth;
    private String workplaceAddress;
    private List<AiNoticeSectionDto> sections;
    private List<AiNoticeProcessDto> processes;
    private List<AiApplicationDocumentDto> documents;
}
