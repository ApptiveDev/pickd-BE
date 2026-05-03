package back.pickd.notice.process;

import back.pickd.notice.notice.Notice;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "notice_processes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Notice notice; // 부모 공고 (FK)

    @Column(name = "process_name", nullable = false, length = 100)
    private String processName; // 전형 트랙 구분명 (예: 공통 전형, 개발자 전형)

    @Column(name = "application_period", length = 255)
    private String applicationPeriod; // 접수 기간

    @Column(name = "document_screen_schedule", length = 255)
    private String documentScreenSchedule; // 서류전형 일정

    @Column(name = "coding_test_schedule", length = 255)
    private String codingTestSchedule; // 코딩테스트 일정 (개발직군용, 없을 시 NULL)

    @Column(name = "written_exam_schedule", length = 255)
    private String writtenExamSchedule; // 필기전형 일정 (일반/공기업용, 없을 시 NULL)

    @Column(name = "interview_schedule", length = 255)
    private String interviewSchedule; // 면접전형 일정

    @Column(name = "announcement_date", length = 255)
    private String announcementDate; // 합격자 발표일

    @Column(name = "join_date", length = 255)
    private String joinDate; // 입사 예정일

    @Column(name = "other_schedules", columnDefinition = "TEXT")
    private String otherSchedules; // 기타 일정

    @Column(name = "schedule_notes", columnDefinition = "TEXT")
    private String scheduleNotes; // 일정 유의사항

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 생성 일시

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정 일시

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public NoticeProcess(Notice notice, String processName, String applicationPeriod,
                         String documentScreenSchedule, String codingTestSchedule,
                         String writtenExamSchedule, String interviewSchedule,
                         String announcementDate, String joinDate,
                         String otherSchedules, String scheduleNotes) {
        this.notice = notice;
        this.processName = processName;
        this.applicationPeriod = applicationPeriod;
        this.documentScreenSchedule = documentScreenSchedule;
        this.codingTestSchedule = codingTestSchedule;
        this.writtenExamSchedule = writtenExamSchedule;
        this.interviewSchedule = interviewSchedule;
        this.announcementDate = announcementDate;
        this.joinDate = joinDate;
        this.otherSchedules = otherSchedules;
        this.scheduleNotes = scheduleNotes;
    }
}
