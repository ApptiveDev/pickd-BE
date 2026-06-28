package back.pickd.notice.dto.response;

import back.pickd.notice.entity.NoticeProcess;
import lombok.Getter;

@Getter
public class NoticeProcessResponse {

    private final Long id;
    private final String processName;
    private final String documentScreenSchedule;
    private final String writtenExamSchedule;
    private final String interviewSchedule;
    private final String joinDate;
    private final String applicationPeriod;
    private final String scheduleNotes;

    public NoticeProcessResponse(NoticeProcess p) {
        this.id = p.getId();
        this.processName = p.getProcessName();
        this.documentScreenSchedule = p.getDocumentScreenSchedule();
        this.writtenExamSchedule = p.getWrittenExamSchedule();
        this.interviewSchedule = p.getInterviewSchedule();
        this.joinDate = p.getJoinDate();
        this.applicationPeriod = p.getApplicationPeriod();
        this.scheduleNotes = p.getScheduleNotes();
    }
}
