package back.pickd.notice.dto.response;

import back.pickd.notice.entity.ApplicationDocument;
import lombok.Getter;

@Getter
public class ApplicationDocumentResponse {

    private final Long id;
    private final String mandatoryDocuments;
    private final String proofDocuments;
    private final String applyMethod;
    private final String applyUrlOrEmail;
    private final String submissionNotes;

    public ApplicationDocumentResponse(ApplicationDocument d) {
        this.id = d.getId();
        this.mandatoryDocuments = d.getMandatoryDocuments();
        this.proofDocuments = d.getProofDocuments();
        this.applyMethod = d.getApplyMethod();
        this.applyUrlOrEmail = d.getApplyUrlOrEmail();
        this.submissionNotes = d.getSubmissionNotes();
    }
}
