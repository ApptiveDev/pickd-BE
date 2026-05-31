package back.pickd.notice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeProcessSaveRequestDto {

    @NotBlank(message = "전형 단계명은 필수입니다.")
    private String processName;

    private String documentScreenSchedule;
    private String writtenExamSchedule;
    private String interviewSchedule;
    private String joinDate;
    private String applicationPeriod;
    private String scheduleNotes;

    @Builder
    public NoticeProcessSaveRequestDto(String processName, String documentScreenSchedule, String writtenExamSchedule,
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
