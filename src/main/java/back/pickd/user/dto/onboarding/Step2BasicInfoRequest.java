package back.pickd.user.dto.onboarding;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class Step2BasicInfoRequest {
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2~10자 사이여야 합니다.")
    private String nickname;

    @NotBlank(message = "거주 지역은 필수입니다.")
    private String currentResidence;

    @NotEmpty(message = "희망 근무 지역을 최소 하나 선택해 주세요.")
    private List<String> desiredLocations;

    private String detailedAddress;
    private String intro;
}
