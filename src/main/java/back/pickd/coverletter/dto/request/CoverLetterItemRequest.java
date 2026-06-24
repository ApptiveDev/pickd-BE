package back.pickd.coverletter.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CoverLetterItemRequest {

    @NotBlank
    private String question;

    private String answer;

    private Integer maxLength;

    private Integer orderIndex;

    private boolean aiGenerated;

    // notice_id 또는 application_id 중 하나만 값이 있어야 함
    private Long noticeId;
    private Long applicationId;
}
