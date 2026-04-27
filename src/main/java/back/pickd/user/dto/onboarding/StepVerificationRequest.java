package back.pickd.user.dto.onboarding;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StepVerificationRequest {
    private String name;
    private String birthDate;
    private String phone;
}
