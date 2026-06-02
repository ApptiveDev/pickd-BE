package back.pickd.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class ExperienceStep3Request {

    @NotEmpty(message = "처리할 중복 후보 결정 목록은 필수입니다.")
    private List<Decision> decisions = new ArrayList<>();

    @Getter
    @NoArgsConstructor
    public static class Decision {
        @NotNull(message = "처리 action은 필수입니다.")
        private ExperienceStep3Action action;

        @Valid
        @NotNull(message = "저장 후보 draft는 필수입니다.")
        private ExperienceDraftRequest draft;
    }
}
