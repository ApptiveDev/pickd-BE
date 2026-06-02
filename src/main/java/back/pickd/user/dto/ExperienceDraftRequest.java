package back.pickd.user.dto;

import back.pickd.user.entity.enums.ExperienceGroup;
import back.pickd.user.entity.enums.ExperienceType;
import back.pickd.user.entity.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class ExperienceDraftRequest {

    @NotBlank(message = "경험 제목은 필수입니다.")
    private String title;

    @NotNull(message = "경험 유형은 필수입니다.")
    private ExperienceType experienceType;

    @NotNull(message = "경험 그룹은 필수입니다.")
    private ExperienceGroup experienceGroup;

    private Status status = Status.COMPLETED;

    private String documentContent;

    private Map<String, Object> attributes = new HashMap<>();

    private List<String> keywords = new ArrayList<>();
}
