package back.pickd.notice.dto.response;

import back.pickd.notice.entity.SectionPreference;
import lombok.Getter;

@Getter
public class SectionPreferenceResponse {

    private final Long id;
    private final String generalPreference;
    private final String additionalPoints;
    private final String veteranPreference;
    private final String disabilityPreference;
    private final String certificatePreference;

    public SectionPreferenceResponse(SectionPreference p) {
        this.id = p.getId();
        this.generalPreference = p.getGeneralPreference();
        this.additionalPoints = p.getAdditionalPoints();
        this.veteranPreference = p.getVeteranPreference();
        this.disabilityPreference = p.getDisabilityPreference();
        this.certificatePreference = p.getCertificatePreference();
    }
}
