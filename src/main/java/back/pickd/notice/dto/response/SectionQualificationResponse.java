package back.pickd.notice.dto.response;

import back.pickd.notice.entity.SectionQualification;
import lombok.Getter;

@Getter
public class SectionQualificationResponse {

    private final Long id;
    private final String generalQualification;
    private final String mandatoryQualification;

    public SectionQualificationResponse(SectionQualification q) {
        this.id = q.getId();
        this.generalQualification = q.getGeneralQualification();
        this.mandatoryQualification = q.getMandatoryQualification();
    }
}
