package back.pickd.notice.dto.response;

import back.pickd.notice.entity.Notice;
import back.pickd.notice.enums.EmploymentType;
import back.pickd.notice.enums.JobCategory;
import lombok.Getter;

import java.util.List;

@Getter
public class NoticeDetailResponse {

    private final Long id;
    private final String companyName;
    private final String noticeName;
    private final JobCategory category;
    private final String startedAt;
    private final String endedAt;
    private final EmploymentType employmentType;
    private final String headcount;
    private final String region1depth;
    private final String workplaceAddress;
    private final String noticeUrl;
    private final List<NoticeSectionResponse> sections;
    private final List<NoticeProcessResponse> processes;
    private final List<ApplicationDocumentResponse> documents;
    private final List<CoverLetterItemResponse> coverLetterItems;

    public NoticeDetailResponse(Notice notice,
                                List<NoticeSectionResponse> sections,
                                List<NoticeProcessResponse> processes,
                                List<ApplicationDocumentResponse> documents,
                                List<CoverLetterItemResponse> coverLetterItems) {
        this.id = notice.getId();
        this.companyName = notice.getCompanyName();
        this.noticeName = notice.getNoticeName();
        this.category = notice.getCategory();
        this.startedAt = notice.getStartedAt();
        this.endedAt = notice.getEndedAt();
        this.employmentType = notice.getEmploymentType();
        this.headcount = notice.getHeadcount();
        this.region1depth = notice.getRegion1depth();
        this.workplaceAddress = notice.getWorkplaceAddress();
        this.noticeUrl = notice.getNoticeUrl();
        this.sections = sections;
        this.processes = processes;
        this.documents = documents;
        this.coverLetterItems = coverLetterItems;
    }
}
