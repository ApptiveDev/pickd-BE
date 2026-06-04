package back.pickd.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 일정 등록 및 수정을 위한 데이터 전송 객체 (Request DTO)
 */
@Schema(description = "Google Calendar 일정 등록/수정 요청")
@Getter
@Setter
@NoArgsConstructor
public class CalendarEventRequest {
    @Schema(description = "일정 제목", example = "Pickd 서류 제출")
    private String summary;

    @Schema(description = "일정 장소", example = "온라인")
    private String location;

    @Schema(description = "일정 설명", example = "백엔드 개발자 공고 서류 제출")
    private String description;

    @Schema(description = "일정 시작 시각")
    private EventTimeDto start;

    @Schema(description = "일정 종료 시각")
    private EventTimeDto end;

    @Schema(description = "Google Calendar 일정 시각 정보")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class EventTimeDto {
        @Schema(description = "ISO-8601 날짜/시간 문자열", example = "2026-06-20T10:00:00+09:00")
        private String dateTime;

        @Schema(description = "IANA 타임존", example = "Asia/Seoul")
        private String timeZone;
    }
}
