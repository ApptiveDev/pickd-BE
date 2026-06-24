package back.pickd.coverletter.dto.response;

import back.pickd.coverletter.entity.CoverLetterItem;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CoverLetterItemResponse {

    private Long id;
    private Long noticeId;
    private Long applicationId;
    private String question;
    private String answer;
    private Integer maxLength;
    private Integer orderIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CoverLetterItemResponse from(CoverLetterItem item) {
        return CoverLetterItemResponse.builder()
                .id(item.getId())
                .noticeId(item.getNotice() != null ? item.getNotice().getId() : null)
                .applicationId(item.getApplication() != null ? item.getApplication().getId() : null)
                .question(item.getQuestion())
                .answer(item.getAnswer())
                .maxLength(item.getMaxLength())
                .orderIndex(item.getOrderIndex())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
