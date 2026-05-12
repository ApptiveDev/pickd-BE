package back.pickd.calendar.service;

import com.google.api.services.calendar.model.Event;

import back.pickd.application.entity.Application;
import back.pickd.application.repository.ApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarAsyncService {
    private final CalendarService calendarService;
    private final ApplicationRepository applicationRepository;
    
    @Async
    public void createEventAsync(Long applicationId, String eventType, Authentication authentication, Event event) {
        try {
            Event createdEvent = calendarService.createEvent(authentication, event);
            Application app = applicationRepository.findById(applicationId).orElseThrow(() -> new RuntimeException("지원 정보 없음"));
            switch (eventType) {
                case "apply":
                    app.setApplyEventId(createdEvent.getId());
                    break;
                case "interview":
                    app.setInterviewEventId(createdEvent.getId());
                    break;
                case "deadline":
                    app.setDeadlineEventId(createdEvent.getId());
                    break;
            }
            applicationRepository.save(app);
        } catch (Exception e) {
            log.error("구글 캘린더 일정 생성 실패", e);
        }
    }

    @Async
    public void updateEventAsync( Authentication authentication, String eventId, Event event) {
        try {
            calendarService.updateEvent(authentication, eventId, event);
        } catch (Exception e) {
            log.error("구글 캘린더 일정 수정 실패", e);
        }
    }

    @Async
    public void deleteEventAsync(Authentication authentication, String eventId) {
        try {
            calendarService.deleteEvent(authentication, eventId);
        } catch (Exception e) {
            log.error("구글 캘린더 일정 삭제 실패", e);
        }
    }
}