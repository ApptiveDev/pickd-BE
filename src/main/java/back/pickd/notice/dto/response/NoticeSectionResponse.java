package back.pickd.notice.dto.response;

import back.pickd.notice.entity.NoticeSection;
import back.pickd.notice.entity.SectionPreference;
import back.pickd.notice.entity.SectionQualification;
import lombok.Getter;

import java.util.List;

@Getter
public class NoticeSectionResponse {

    private final Long id;
    private final String sectionName;
    private final String jobTitle;
    private final String responsibilities;
    private final String headcount;
    private final String workplace;
    private final List<SectionQualificationResponse> qualifications;
    private final List<SectionPreferenceResponse> preferences;

    public NoticeSectionResponse(NoticeSection section,
                                 List<SectionQualification> qualifications,
                                 List<SectionPreference> preferences) {
        this.id = section.getId();
        this.sectionName = section.getSectionName();
        this.jobTitle = section.getJobTitle();
        this.responsibilities = section.getResponsibilities();
        this.headcount = section.getHeadcount();
        this.workplace = section.getWorkplace();
        this.qualifications = qualifications.stream()
                .map(SectionQualificationResponse::new)
                .toList();
        this.preferences = preferences.stream()
                .map(SectionPreferenceResponse::new)
                .toList();
    }
}
