package back.pickd.user.dto.onboarding;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Step1TermsRequest {
    private boolean serviceAgreed;
    private boolean privacyAgreed;
    private boolean marketingAgreed;
    private boolean pushAgreed;
}
