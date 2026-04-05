package back.pickd.calendar.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 일정 등록 및 수정을 위한 데이터 전송 객체 (Request DTO)
 */
@Getter
@Setter
@NoArgsConstructor
public class CalendarEventRequest {
    private String summary;
    private String location;
    private String description;
    private EventTimeDto start;
    private EventTimeDto end;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class EventTimeDto {
        private String dateTime;
        private String timeZone;
    }
}
