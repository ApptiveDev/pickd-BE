package back.pickd.notice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "application_documents")
public class ApplicationDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @Column(name = "mandatory_documents", columnDefinition = "TEXT")
    private String mandatoryDocuments;

    @Column(name = "proof_documents", columnDefinition = "TEXT")
    private String proofDocuments;

    @Column(name = "apply_method")
    private String applyMethod;

    @Column(name = "apply_url_or_email")
    private String applyUrlOrEmail;

    @Column(name = "submission_notes", columnDefinition = "TEXT")
    private String submissionNotes;

    @Builder
    public ApplicationDocument(Notice notice, String mandatoryDocuments, String proofDocuments,
                               String applyMethod, String applyUrlOrEmail, String submissionNotes) {
        this.notice = notice;
        this.mandatoryDocuments = mandatoryDocuments;
        this.proofDocuments = proofDocuments;
        this.applyMethod = applyMethod;
        this.applyUrlOrEmail = applyUrlOrEmail;
        this.submissionNotes = submissionNotes;
    }
}
