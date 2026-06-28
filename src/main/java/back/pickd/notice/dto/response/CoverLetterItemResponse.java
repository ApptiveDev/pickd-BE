package back.pickd.notice.dto.response;

import back.pickd.coverletter.entity.CoverLetterItem;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CoverLetterItemResponse {

    private final Long id;
    private final String question;
    private final String answer;
    private final Integer maxLength;
    private final Integer orderIndex;
    private final boolean aiGenerated;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CoverLetterItemResponse(CoverLetterItem item) {
        this.id = item.getId();
        this.question = item.getQuestion();
        this.answer = item.getAnswer();
        this.maxLength = item.getMaxLength();
        this.orderIndex = item.getOrderIndex();
        this.aiGenerated = item.isAiGenerated();
        this.createdAt = item.getCreatedAt();
        this.updatedAt = item.getUpdatedAt();
    }
}
