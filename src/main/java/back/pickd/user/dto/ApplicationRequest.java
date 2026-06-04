package back.pickd.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Schema(description = "지원 현황 등록/수정 요청")
@Getter
@Setter
public class ApplicationRequest {

    @Schema(description = "지원 기업명", example = "Pickd")
    private String company;

    @Schema(description = "채용 공고명 또는 직무명", example = "백엔드 개발자")
    private String jobTitle;

    @Schema(description = "지원 포지션", example = "Backend Engineer")
    private String position;

    @Schema(description = "산업군", example = "IT")
    private String industry;

    @Schema(description = "지원 상태", example = "DOCUMENT_SUBMITTED")
    private String status;

    @Schema(description = "지원 관련 메모", example = "서류 제출 완료, 코딩테스트 준비 필요")
    private String memo;

    @Schema(description = "지원 제출 예정/완료 일시. 값이 있으면 Google Calendar 제출 일정이 생성됩니다.", example = "2026-06-10T10:00:00")
    private LocalDateTime applyDate;

    @Schema(description = "지원 마감 일시. 값이 있으면 Google Calendar 마감 일정이 생성됩니다.", example = "2026-06-20T23:59:00")
    private LocalDateTime deadlineDate;
}
