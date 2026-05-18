package back.pickd.notice.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiNoticeProcessDto {
    @JsonProperty("process_name")
    private String processName;

    @JsonProperty("document_screen_schedule")
    private String documentScreenSchedule;

    @JsonProperty("written_exam_schedule")
    private String writtenExamSchedule;

    @JsonProperty("interview_schedule")
    private String interviewSchedule;

    @JsonProperty("join_date")
    private String joinDate;

    @JsonProperty("application_period")
    private String applicationPeriod;

    @JsonProperty("schedule_notes")
    private String scheduleNotes;

    @Builder
    public AiNoticeProcessDto(String processName, String documentScreenSchedule, String writtenExamSchedule,
                              String interviewSchedule, String joinDate, String applicationPeriod, String scheduleNotes) {
        this.processName = processName;
        this.documentScreenSchedule = documentScreenSchedule;
        this.writtenExamSchedule = writtenExamSchedule;
        this.interviewSchedule = interviewSchedule;
        this.joinDate = joinDate;
        this.applicationPeriod = applicationPeriod;
        this.scheduleNotes = scheduleNotes;
    }
}
