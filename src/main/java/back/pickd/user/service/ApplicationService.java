package back.pickd.user.service;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.client.util.DateTime;

import back.pickd.user.entity.Application;
import back.pickd.user.dto.ApplicationRequest;
import back.pickd.user.repository.ApplicationRepository;
import back.pickd.calendar.service.CalendarService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final CalendarService calendarService;

    private Event buildEvent(String type, String company, String jobTitle, LocalDateTime dateTime) {
        Event event = new Event();
        event.setSummary(type + " " + company + " " + jobTitle);

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
        app.setDeadlineDate(dto.getDeadlineDate());
        app.setPosition(dto.getPosition());
        app.setIndustry(dto.getIndustry());
        app.setStatus(dto.getStatus());
        app.setMemo(dto.getMemo());

        applicationRepository.save(app);

        if (dto.getApplyDate() != null) {
            Event event = buildEvent("[지원]", dto.getCompany(), dto.getJobTitle(), dto.getApplyDate());
            Event created = calendarService.createEvent(auth, event);
            app.setApplyEventId(created.getId());
        }
        if (dto.getDeadlineDate() != null) {
            Event event = buildEvent("[마감]", dto.getCompany(), dto.getJobTitle(), dto.getDeadlineDate());
            Event created = calendarService.createEvent(auth, event);
            app.setDeadlineEventId(created.getId());
        }
        applicationRepository.save(app);
    }

    @Transactional
    public void deleteApplication(Long id, Authentication auth) throws Exception {
        Application app = applicationRepository.findById(id).orElseThrow();

        if (app.getApplyEventId() != null) {
            calendarService.deleteEvent(auth, app.getApplyEventId());
        }
        if (app.getDeadlineEventId() != null) {
            calendarService.deleteEvent(auth, app.getDeadlineEventId());
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

        if (dto.getApplyDate() != null) {
            Event event = buildEvent("[지원]", dto.getCompany(), dto.getJobTitle(), dto.getApplyDate());
            if (app.getApplyEventId() != null) {
                calendarService.updateEvent(auth, app.getApplyEventId(), event);
            } else {
                Event created = calendarService.createEvent(auth, event);
                app.setApplyEventId(created.getId());
            }
        } else {
            if (app.getApplyEventId() != null) {
                calendarService.deleteEvent(auth, app.getApplyEventId());
                app.setApplyEventId(null);
            }
        }

        if (dto.getDeadlineDate() != null) {
            Event event = buildEvent("[마감]", dto.getCompany(), dto.getJobTitle(), dto.getDeadlineDate());

            if (app.getDeadlineEventId() != null) {
                calendarService.updateEvent(auth, app.getDeadlineEventId(), event);
            } else {
                Event created = calendarService.createEvent(auth, event);
                app.setDeadlineEventId(created.getId());
            }
        } else {
            if (app.getDeadlineEventId() != null) {
                calendarService.deleteEvent(auth, app.getDeadlineEventId());
                app.setDeadlineEventId(null);
            }
        }
        applicationRepository.save(app);
    }
}