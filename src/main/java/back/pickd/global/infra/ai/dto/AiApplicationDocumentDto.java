package back.pickd.global.infra.ai.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AiApplicationDocumentDto {
    private String mandatoryDocuments;
    private String proofDocuments;
    private String applyMethod;
    private String applyUrlOrEmail;
    private String submissionNotes;
}
