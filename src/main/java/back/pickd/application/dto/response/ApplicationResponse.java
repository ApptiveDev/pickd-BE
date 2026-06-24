package back.pickd.application.dto.response;

import back.pickd.application.entity.Application;
import back.pickd.application.enums.ApplicationStatus;
import back.pickd.document.entity.Document;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ApplicationResponse {

    private Long id;
    private Long noticeId;
    private List<TodoResponse> todos;
    private String company;
    private String jobTitle;
    private String position;
    private String industry;
    private ApplicationStatus status;   // @JsonValue → "지원 예정" 형태로 직렬화
    private String memo;
    private LocalDateTime applyDate;
    private LocalDateTime interviewDate;
    private LocalDateTime deadlineDate;
    private String applyEventId;
    private String interviewEventId;
    private String deadlineEventId;
    private boolean important;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DocumentSummary> documents;

    public static ApplicationResponse from(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .noticeId(app.getNotice() != null ? app.getNotice().getId() : null)
                .todos(app.getTodos().stream().map(TodoResponse::from).toList())
                .company(app.getCompany())
                .jobTitle(app.getJobTitle())
                .position(app.getPosition())
                .industry(app.getIndustry())
                .status(app.getStatus())
                .memo(app.getMemo())
                .applyDate(app.getApplyDate())
                .interviewDate(app.getInterviewDate())
                .deadlineDate(app.getDeadlineDate())
                .applyEventId(app.getApplyEventId())
                .interviewEventId(app.getInterviewEventId())
                .deadlineEventId(app.getDeadlineEventId())
                .important(app.isImportant())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .documents(app.getDocuments().stream().map(DocumentSummary::from).toList())
                .build();
    }

    @Getter
    @Builder
    public static class DocumentSummary {
        private Long id;
        private String title;
        private String company;
        private String type;
        private String status;
        private Integer progress;

        public static DocumentSummary from(Document doc) {
            return DocumentSummary.builder()
                    .id(doc.getId())
                    .title(doc.getTitle())
                    .company(doc.getCompany())
                    .type(doc.getType() != null ? doc.getType().getLabel() : null)
                    .status(doc.getStatus() != null ? doc.getStatus().getLabel() : null)
                    .progress(doc.getProgress())
                    .build();
        }
    }
}
