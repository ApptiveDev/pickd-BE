package back.pickd.application.service;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.client.util.DateTime;

import back.pickd.application.entity.Application;
import back.pickd.application.repository.ApplicationRepository;
import back.pickd.application.dto.request.ApplicationRequest;
import back.pickd.calendar.service.CalendarAsyncService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final CalendarAsyncService calendarAsyncService;

    private Event buildEvent(String type, String company, String jobTitle, LocalDateTime dateTime) {
        Event event = new Event();
        event.setSummary(company + " " + jobTitle + " " + type);

        DateTime googleDateTime = new DateTime(java.sql.Timestamp.valueOf(dateTime));

        EventDateTime start = new EventDateTime()
                .setDateTime(googleDateTime)
                .setTimeZone("Asia/Seoul");

        EventDateTime end = new EventDateTime()
                .setDateTime(googleDateTime)
                .setTimeZone("Asia/Seoul");
        event.setStart(start);
        event.setEnd(end);

        return event;
    }

    @Transactional
    public void addApplication(ApplicationRequest dto, Authentication auth) throws Exception {
        Application app = new Application();
        app.setCompany(dto.getCompany());
        app.setJobTitle(dto.getJobTitle());
        app.setApplyDate(dto.getApplyDate());
        app.setInterviewDate(dto.getInterviewDate());
        app.setDeadlineDate(dto.getDeadlineDate());
        app.setPosition(dto.getPosition());
        app.setIndustry(dto.getIndustry());
        app.setStatus(dto.getStatus());
        app.setMemo(dto.getMemo());

        applicationRepository.save(app);

        if (dto.getApplyDate() != null) {
            Event event = buildEvent("제출", dto.getCompany(), dto.getJobTitle(), dto.getApplyDate());
            calendarAsyncService.createEventAsync(app.getId(), "apply", auth, event);
        }
        if (dto.getInterviewDate() != null) {
            Event event = buildEvent("면접", dto.getCompany(), dto.getJobTitle(), dto.getInterviewDate());
            calendarAsyncService.createEventAsync(app.getId(), "interview", auth, event);
        }
        if (dto.getDeadlineDate() != null) {
            Event event = buildEvent("마감", dto.getCompany(), dto.getJobTitle(), dto.getDeadlineDate());
            calendarAsyncService.createEventAsync(app.getId(), "deadline", auth, event);
        }
        applicationRepository.save(app);
    }

    @Transactional
    public void deleteApplication(Long id, Authentication auth) throws Exception {
        Application app = applicationRepository.findById(id).orElseThrow();

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
        Application app = applicationRepository.findById(id).orElseThrow();

        app.setCompany(dto.getCompany());
        app.setJobTitle(dto.getJobTitle());
        app.setPosition(dto.getPosition());
        app.setIndustry(dto.getIndustry());
        app.setStatus(dto.getStatus());
        app.setMemo(dto.getMemo());
        app.setApplyDate(dto.getApplyDate());
        app.setDeadlineDate(dto.getDeadlineDate());
        app.setInterviewDate(dto.getInterviewDate());

        if (dto.getApplyDate() != null) {
            Event event = buildEvent("제출", dto.getCompany(), dto.getJobTitle(), dto.getApplyDate());
            if (app.getApplyEventId() != null) {
                calendarAsyncService.updateEventAsync(auth, app.getApplyEventId(), event);
            } else {
                calendarAsyncService.createEventAsync(app.getId(), "apply", auth, event);
            }
        } else {
            if (app.getApplyEventId() != null) {
                calendarAsyncService.deleteEventAsync(auth, app.getApplyEventId());
                app.setApplyEventId(null);
            }
        }
        if (dto.getInterviewDate() != null) {
            Event event = buildEvent("면접", dto.getCompany(), dto.getJobTitle(), dto.getInterviewDate());

            if (app.getInterviewEventId() != null) {
                calendarAsyncService.updateEventAsync(auth, app.getInterviewEventId(), event);
            } else {
                calendarAsyncService.createEventAsync(app.getId(), "interview", auth, event);
            }
        } else {
            if (app.getInterviewEventId() != null) {
                calendarAsyncService.deleteEventAsync(auth, app.getInterviewEventId());
                app.setInterviewEventId(null);
            }
        }
        if (dto.getDeadlineDate() != null) {
            Event event = buildEvent("마감", dto.getCompany(), dto.getJobTitle(), dto.getDeadlineDate());

            if (app.getDeadlineEventId() != null) {
                calendarAsyncService.updateEventAsync(auth, app.getDeadlineEventId(), event);
            } else {
                calendarAsyncService.createEventAsync(app.getId(), "deadline", auth, event);
            }
        } else {
            if (app.getDeadlineEventId() != null) {
                calendarAsyncService.deleteEventAsync(auth, app.getDeadlineEventId());
                app.setDeadlineEventId(null);
            }
        }
        applicationRepository.save(app);
    }
}