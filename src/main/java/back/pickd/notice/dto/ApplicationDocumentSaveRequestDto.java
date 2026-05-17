package back.pickd.notice.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationDocumentSaveRequestDto {

    private String mandatoryDocuments;
    private String proofDocuments;
    private String applyMethod;
    private String applyUrlOrEmail;
    private String submissionNotes;

    @Builder
    public ApplicationDocumentSaveRequestDto(String mandatoryDocuments, String proofDocuments,
                                             String applyMethod, String applyUrlOrEmail, String submissionNotes) {
        this.mandatoryDocuments = mandatoryDocuments;
        this.proofDocuments = proofDocuments;
        this.applyMethod = applyMethod;
        this.applyUrlOrEmail = applyUrlOrEmail;
        this.submissionNotes = submissionNotes;
    }
}
