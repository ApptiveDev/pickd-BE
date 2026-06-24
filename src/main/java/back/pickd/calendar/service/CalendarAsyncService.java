package back.pickd.calendar.service;

import back.pickd.application.entity.Application;
import back.pickd.application.entity.Todo;
import back.pickd.application.repository.ApplicationRepository;
import back.pickd.application.repository.TodoRepository;
import com.google.api.services.calendar.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarAsyncService {
    private final TodoRepository todoRepository;
    private final CalendarService calendarService;
    private final ApplicationRepository applicationRepository;

    @Async
    @Transactional
    public void createEventAsync(Long applicationId, String eventType, Authentication authentication, Event event) {
        try {
            Event createdEvent = calendarService.createEvent(authentication, event);

            Application app = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("지원 정보 없음"));

            switch (eventType) {
                case "apply":
                    app.assignApplyEventId(createdEvent.getId());
                    break;
                case "interview":
                    app.assignInterviewEventId(createdEvent.getId());
                    break;
                case "deadline":
                    app.assignDeadlineEventId(createdEvent.getId());
                    break;
                default:
                    break;
            }

            applicationRepository.save(app);
        } catch (Exception e) {
            log.error("구글 캘린더 일정 생성 실패", e);
        }
    }

    @Async
    public void updateEventAsync(Authentication authentication, String eventId, Event event) {
        try {
            calendarService.updateEvent(authentication, eventId, event);
        } catch (Exception e) {
            log.error("구글 캘린더 일정 수정 실패", e);
        }
    }

    @Async
    public void deleteEventAsync(Authentication authentication, String eventId) {
        try {
            if (eventId == null || eventId.isBlank()) {
                return;
            }

            calendarService.deleteEvent(authentication, eventId);
        } catch (Exception e) {
            log.error("구글 캘린더 일정 삭제 실패", e);
        }
    }

    @Async
    @Transactional
    public void createTodoEventAsync(Long todoId, Authentication authentication) {
        try {
            Todo todo = todoRepository.findById(todoId)
                    .orElseThrow(() -> new RuntimeException("할 일을 찾을 수 없습니다."));

            if (todo.getDueDateTime() == null) {
                return;
            }

            Event createdEvent = calendarService.createTodoEvent(authentication, todo);

            if (createdEvent != null && createdEvent.getId() != null) {
                todo.assignCalendarEventId(createdEvent.getId());
                todoRepository.save(todo);
            }
        } catch (Exception e) {
            log.error("할 일 구글 캘린더 생성 실패", e);
        }
    }
}