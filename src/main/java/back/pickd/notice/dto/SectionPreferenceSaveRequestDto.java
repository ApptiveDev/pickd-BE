package back.pickd.notice.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SectionPreferenceSaveRequestDto {

    private String generalPreference;
    private String additionalPoints;
    private String veteranPreference;
    private String disabilityPreference;
    private String certificatePreference;

    @Builder
    public SectionPreferenceSaveRequestDto(String generalPreference, String additionalPoints, String veteranPreference,
                                           String disabilityPreference, String certificatePreference) {
        this.generalPreference = generalPreference;
        this.additionalPoints = additionalPoints;
        this.veteranPreference = veteranPreference;
        this.disabilityPreference = disabilityPreference;
        this.certificatePreference = certificatePreference;
    }
}
