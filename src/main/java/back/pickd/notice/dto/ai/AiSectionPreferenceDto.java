package back.pickd.notice.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiSectionPreferenceDto {
    @JsonProperty("general_preference")
    private String generalPreference;

    @JsonProperty("additional_points")
    private String additionalPoints;

    @JsonProperty("veteran_preference")
    private String veteranPreference;

    @JsonProperty("disability_preference")
    private String disabilityPreference;

    @JsonProperty("certificate_preference")
    private String certificatePreference;

    @Builder
    public AiSectionPreferenceDto(String generalPreference, String additionalPoints, String veteranPreference,
                                  String disabilityPreference, String certificatePreference) {
        this.generalPreference = generalPreference;
        this.additionalPoints = additionalPoints;
        this.veteranPreference = veteranPreference;
        this.disabilityPreference = disabilityPreference;
        this.certificatePreference = certificatePreference;
    }
}
