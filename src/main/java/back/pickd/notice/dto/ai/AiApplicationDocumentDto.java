package back.pickd.notice.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiApplicationDocumentDto {
    @JsonProperty("mandatory_documents")
    private String mandatoryDocuments;

    @JsonProperty("proof_documents")
    private String proofDocuments;

    @JsonProperty("apply_method")
    private String applyMethod;

    @JsonProperty("apply_url_or_email")
    private String applyUrlOrEmail;

    @JsonProperty("submission_notes")
    private String submissionNotes;

    @Builder
    public AiApplicationDocumentDto(String mandatoryDocuments, String proofDocuments, String applyMethod,
                                    String applyUrlOrEmail, String submissionNotes) {
        this.mandatoryDocuments = mandatoryDocuments;
        this.proofDocuments = proofDocuments;
        this.applyMethod = applyMethod;
        this.applyUrlOrEmail = applyUrlOrEmail;
        this.submissionNotes = submissionNotes;
    }
}
