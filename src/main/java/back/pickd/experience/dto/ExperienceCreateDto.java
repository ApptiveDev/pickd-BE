package back.pickd.experience.dto;

import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.experience.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ExperienceCreateDto {

    private ExperienceCreateDto() {
    }

    @Getter
    @NoArgsConstructor
    public static class Request {

        @NotBlank(message = "경험 제목은 필수입니다.")
        private String title;

        @NotNull(message = "경험 유형은 필수입니다.")
        private ExperienceType experienceType;

        @NotNull(message = "경험 그룹은 필수입니다.")
        private ExperienceGroup experienceGroup;

        @NotNull(message = "상태는 필수입니다.")
        private Status status;

        private String documentContent;
        private Map<String, Object> attributes = new HashMap<>();
        private List<String> keywords = new ArrayList<>();
        private List<LinkRequest> links = new ArrayList<>();
        private boolean forceCreate;
    }

    @Getter
    @NoArgsConstructor
    public static class LinkRequest {
        private String title;
        private String url;
        private String materialType;
        private Integer documentPosition;
    }

    @Getter
    @AllArgsConstructor
    public static class Response {
        private final String id;
    }
}
