package back.pickd.notice.dto.response;

import back.pickd.notice.entity.Notice;
import back.pickd.notice.enums.EmploymentType;
import back.pickd.notice.enums.JobCategory;
import lombok.Getter;

@Getter
public class NoticeListResponse {

    private final Long id;
    private final String companyName;
    private final String noticeName;
    private final JobCategory category;
    private final String startedAt;
    private final String endedAt;
    private final EmploymentType employmentType;
    private final String region1depth;
    private final String noticeUrl;

    public NoticeListResponse(Notice notice) {
        this.id = notice.getId();
        this.companyName = notice.getCompanyName();
        this.noticeName = notice.getNoticeName();
        this.category = notice.getCategory();
        this.startedAt = notice.getStartedAt();
        this.endedAt = notice.getEndedAt();
        this.employmentType = notice.getEmploymentType();
        this.region1depth = notice.getRegion1depth();
        this.noticeUrl = notice.getNoticeUrl();
    }
}
