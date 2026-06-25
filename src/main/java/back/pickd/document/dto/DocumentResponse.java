package back.pickd.document.dto;

import back.pickd.document.entity.Document;
import back.pickd.document.enums.DocumentStatus;
import back.pickd.document.enums.DocumentType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DocumentResponse {

    private final Long id;
    private final Long applicationId;
    private final String title;
    private final String company;
    private final DocumentType type;
    private final DocumentStatus status;
    private final Integer progress;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public DocumentResponse(Document document) {
        this.id = document.getId();
        this.applicationId = document.getApplication() != null ? document.getApplication().getId() : null;
        this.title = document.getTitle();
        this.company = document.getCompany();
        this.type = document.getType();
        this.status = document.getStatus();
        this.progress = document.getProgress();
        this.content = document.getContent();
        this.createdAt = document.getCreatedAt();
        this.updatedAt = document.getUpdatedAt();
    }
}
