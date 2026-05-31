package back.pickd.global.infra.ai.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AiNoticeProcessDto {
    private String processName;
    private String documentScreenSchedule;
    private String writtenExamSchedule;
    private String interviewSchedule;
    private String joinDate;
    private String applicationPeriod;
    private String scheduleNotes;
}
