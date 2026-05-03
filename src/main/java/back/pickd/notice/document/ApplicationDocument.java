package back.pickd.notice.document;

import back.pickd.notice.notice.Notice;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Notice notice; // 부모 공고 (FK)

    @Column(name = "target_type")
    private String targetType; // 대상 구분 (예: COMMON, EXPERIENCED 등)

    @Column(name = "apply_method")
    private String applyMethod; // 지원 방법 (예: 온라인, 이메일 등)

    @Column(name = "apply_url_or_email")
    private String applyUrlOrEmail; // 지원 URL 또는 이메일 주소

    @Column(name = "mandatory_documents", columnDefinition = "TEXT")
    private String mandatoryDocuments; // 필수 제출 서류

    @Column(name = "optional_documents", columnDefinition = "TEXT")
    private String optionalDocuments; // 선택 제출 서류

    @Column(name = "proof_documents", columnDefinition = "TEXT")
    private String proofDocuments; // 증빙 서류 (예: 경력증명서, 자격증 사본 등)

    @Column(name = "online_form_items", columnDefinition = "TEXT")
    private String onlineFormItems; // 온라인 입력 항목 (예: 자기소개서 문항 등)

    @Column(name = "attachment_guide", columnDefinition = "TEXT")
    private String attachmentGuide; // 파일 첨부 안내 (형식, 용량 제한 등)

    @Column(name = "submission_format")
    private String submissionFormat; // 제출 형식 (예: PDF, HWP 등)

    @Column(name = "submission_notes", columnDefinition = "TEXT")
    private String submissionNotes; // 제출 관련 유의사항

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 생성 일시

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정 일시

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public ApplicationDocument(Notice notice, String targetType, String applyMethod,
                               String applyUrlOrEmail, String mandatoryDocuments,
                               String optionalDocuments, String proofDocuments,
                               String onlineFormItems, String attachmentGuide,
                               String submissionFormat, String submissionNotes) {
        this.notice = notice;
        this.targetType = targetType;
        this.applyMethod = applyMethod;
        this.applyUrlOrEmail = applyUrlOrEmail;
        this.mandatoryDocuments = mandatoryDocuments;
        this.optionalDocuments = optionalDocuments;
        this.proofDocuments = proofDocuments;
        this.onlineFormItems = onlineFormItems;
        this.attachmentGuide = attachmentGuide;
        this.submissionFormat = submissionFormat;
        this.submissionNotes = submissionNotes;
    }
}
