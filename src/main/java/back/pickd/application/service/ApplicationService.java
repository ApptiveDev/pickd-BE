package back.pickd.application.service;

import back.pickd.application.dto.request.ApplicationRequest;
import back.pickd.application.dto.response.ApplicationResponse;
import back.pickd.application.entity.Application;
import back.pickd.application.enums.ApplicationStatus;
import back.pickd.application.repository.ApplicationRepository;
import back.pickd.calendar.service.CalendarAsyncService;
import back.pickd.notice.entity.Notice;
import back.pickd.notice.enums.JobCategory;
import back.pickd.notice.repository.NoticeRepository;
import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final NoticeRepository noticeRepository;
    private final CalendarAsyncService calendarAsyncService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplications(Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        return applicationRepository.findAllByUserOrderByIdDesc(user)
                .stream().map(ApplicationResponse::from).toList();
    }

    @Transactional
    public void addApplication(ApplicationRequest dto, Authentication auth) throws Exception {
        User user = userService.findByEmail(auth.getName());
        ApplicationStatus status = dto.getStatus();

        Notice notice;
        if (dto.getNoticeId() != null) {
            // 기존 AI 분석 공고와 연결
            notice = noticeRepository.findByIdAndUser(dto.getNoticeId(), user)
                    .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));
        } else {
            // 수기 입력: 최소 정보로 Notice 자동 생성
            String companyName = dto.getCompany() != null ? dto.getCompany() : "미입력";
            String noticeName  = dto.getJobTitle() != null ? dto.getJobTitle() : "미입력";
            JobCategory category = dto.getCategory() != null ? dto.getCategory() : JobCategory.FULL_TIME;
            String startedAt = dto.getStartedAt() != null ? dto.getStartedAt()
                    : java.time.LocalDate.now().toString();

            notice = noticeRepository.save(
                    Notice.builder()
                            .user(user)
                            .companyName(companyName)
                            .noticeName(noticeName)
                            .category(category)
                            .startedAt(startedAt)
                            .endedAt(dto.getEndedAt())
                            .build()
            );
        }

        Application app = Application.builder()
                .user(user)
                .notice(notice)
                .company(dto.getCompany())
                .jobTitle(dto.getJobTitle())
                .position(dto.getPosition())
                .industry(dto.getIndustry())
                .status(status)
                .important(dto.isImportant())
                .memo(dto.getMemo())
                .applyDate(dto.getApplyDate())
                .interviewDate(dto.getInterviewDate())
                .deadlineDate(dto.getDeadlineDate())
                .build();

        applicationRepository.save(app);

        if (status.needsApplyEvent() && dto.getApplyDate() != null) {
            Event event = buildEvent("제출", dto.getCompany(), dto.getJobTitle(), dto.getApplyDate());
            calendarAsyncService.createEventAsync(app.getId(), "apply", auth, event);
        }
        if (status.needsInterviewEvent() && dto.getInterviewDate() != null) {
            Event event = buildEvent("면접", dto.getCompany(), dto.getJobTitle(), dto.getInterviewDate());
            calendarAsyncService.createEventAsync(app.getId(), "interview", auth, event);
        }
        if (status.needsDeadlineEvent() && dto.getDeadlineDate() != null) {
            Event event = buildEvent("마감", dto.getCompany(), dto.getJobTitle(), dto.getDeadlineDate());
            calendarAsyncService.createEventAsync(app.getId(), "deadline", auth, event);
        }

    }

    @Transactional
    public void deleteApplication(Long id, Authentication auth) throws Exception {
        User user = userService.findByEmail(auth.getName());
        Application app = applicationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("지원 공고를 찾을 수 없습니다."));

        if (app.getApplyEventId() != null) {
            calendarAsyncService.deleteEventAsync(auth, app.getApplyEventId());
        }
        if (app.getDeadlineEventId() != null) {
            calendarAsyncService.deleteEventAsync(auth, app.getDeadlineEventId());
        }
        if (app.getInterviewEventId() != null) {
            calendarAsyncService.deleteEventAsync(auth, app.getInterviewEventId());
        }
        applicationRepository.delete(app);
    }

    @Transactional
    public void updateApplication(Long id, ApplicationRequest dto, Authentication auth) throws Exception {
        User user = userService.findByEmail(auth.getName());
        Application app = applicationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("지원 공고를 찾을 수 없습니다."));

        ApplicationStatus status = dto.getStatus();

        app.update(
                dto.getCompany(), dto.getJobTitle(), dto.getPosition(), dto.getIndustry(),
                status, dto.isImportant(), dto.getMemo(),
                dto.getApplyDate(), dto.getInterviewDate(), dto.getDeadlineDate()
        );

        if (status.needsApplyEvent() && dto.getApplyDate() != null) {
            Event event = buildEvent("제출", dto.getCompany(), dto.getJobTitle(), dto.getApplyDate());
            if (app.getApplyEventId() != null) {
                calendarAsyncService.updateEventAsync(auth, app.getApplyEventId(), event);
            } else {
                calendarAsyncService.createEventAsync(app.getId(), "apply", auth, event);
            }
        } else if (app.getApplyEventId() != null) {
            calendarAsyncService.deleteEventAsync(auth, app.getApplyEventId());
            app.clearApplyEventId();
        }

        if (status.needsInterviewEvent() && dto.getInterviewDate() != null) {
            Event event = buildEvent("면접", dto.getCompany(), dto.getJobTitle(), dto.getInterviewDate());
            if (app.getInterviewEventId() != null) {
                calendarAsyncService.updateEventAsync(auth, app.getInterviewEventId(), event);
            } else {
                calendarAsyncService.createEventAsync(app.getId(), "interview", auth, event);
            }
        } else if (app.getInterviewEventId() != null) {
            calendarAsyncService.deleteEventAsync(auth, app.getInterviewEventId());
            app.clearInterviewEventId();
        }

        if (status.needsDeadlineEvent() && dto.getDeadlineDate() != null) {
            Event event = buildEvent("마감", dto.getCompany(), dto.getJobTitle(), dto.getDeadlineDate());
            if (app.getDeadlineEventId() != null) {
                calendarAsyncService.updateEventAsync(auth, app.getDeadlineEventId(), event);
            } else {
                calendarAsyncService.createEventAsync(app.getId(), "deadline", auth, event);
            }
        } else if (app.getDeadlineEventId() != null) {
            calendarAsyncService.deleteEventAsync(auth, app.getDeadlineEventId());
            app.clearDeadlineEventId();
        }

        applicationRepository.save(app);
    }

    private Event buildEvent(String type, String company, String jobTitle, LocalDateTime dateTime) {
        Event event = new Event();
        event.setSummary(company + " " + jobTitle + " " + type);
        DateTime googleDateTime = new DateTime(java.sql.Timestamp.valueOf(dateTime));
        EventDateTime eventDateTime = new EventDateTime()
                .setDateTime(googleDateTime)
                .setTimeZone("Asia/Seoul");
        event.setStart(eventDateTime);
        event.setEnd(eventDateTime);
        return event;
    }
}
