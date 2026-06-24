package back.pickd.notice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notice_processes")
public class NoticeProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @Column(name = "process_name", nullable = false)
    private String processName;

    @Column(name = "document_screen_schedule")
    private String documentScreenSchedule;

    @Column(name = "written_exam_schedule")
    private String writtenExamSchedule;

    @Column(name = "interview_schedule")
    private String interviewSchedule;

    @Column(name = "join_date")
    private String joinDate;

    @Column(name = "application_period")
    private String applicationPeriod;

    @Column(name = "schedule_notes", columnDefinition = "TEXT")
    private String scheduleNotes;

    @Builder
    public NoticeProcess(Notice notice, String processName, String documentScreenSchedule,
                         String writtenExamSchedule, String interviewSchedule, String joinDate,
                         String applicationPeriod, String scheduleNotes) {
        this.notice = notice;
        this.processName = processName;
        this.documentScreenSchedule = documentScreenSchedule;
        this.writtenExamSchedule = writtenExamSchedule;
        this.interviewSchedule = interviewSchedule;
        this.joinDate = joinDate;
        this.applicationPeriod = applicationPeriod;
        this.scheduleNotes = scheduleNotes;
    }
}
