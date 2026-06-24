package back.pickd.global.infra.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AiExperiencePresetSchema {

    @JsonProperty("experience_group")
    private final String experienceGroup;

    @JsonProperty("experience_type")
    private final String experienceType;

    @JsonProperty("experience_type_name")
    private final String experienceTypeName;

    private final List<Field> fields;

    @Getter
    @AllArgsConstructor
    public static class Field {
        private final String key;
        private final String label;
    }
}
