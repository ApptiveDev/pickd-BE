package back.pickd.application.service;

import back.pickd.application.dto.request.TodoRequest;
import back.pickd.application.dto.response.TodoResponse;
import back.pickd.application.entity.Application;
import back.pickd.application.entity.Todo;
import back.pickd.application.repository.ApplicationRepository;
import back.pickd.application.repository.TodoRepository;
import back.pickd.calendar.service.CalendarAsyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;
    private final ApplicationRepository applicationRepository;
    private final CalendarAsyncService calendarAsyncService;

    public TodoResponse addTodo(TodoRequest dto, Authentication authentication) {
        Todo todo = new Todo();
        todo.setTitle(dto.getTitle());
        todo.setCompleted(false);
        todo.setMemo(dto.getMemo());

        LocalDateTime dueDateTime = parseDueDateTime(dto.getDueDateTime());
        todo.setDueDateTime(dueDateTime);

        if (dto.getApplicationId() != null) {
            Application application = applicationRepository
                    .findById(dto.getApplicationId())
                    .orElseThrow(() -> new RuntimeException("공고를 찾을 수 없습니다."));

            todo.setApplication(application);
        }

        Todo saved = todoRepository.save(todo);

        if (saved.getDueDateTime() != null) {
            calendarAsyncService.createTodoEventAsync(saved.getId(), authentication);
        }

        return TodoResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> getTodos() {
        return todoRepository.findAllWithApplication()
                .stream()
                .map(TodoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> getTodosByApplication(Long applicationId) {
        return todoRepository.findByApplicationId(applicationId)
                .stream()
                .map(TodoResponse::from)
                .toList();
    }

    public void toggleTodo(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("할 일을 찾을 수 없습니다."));

        todo.setCompleted(!todo.isCompleted());
    }

    public void deleteTodo(Long id, Authentication authentication) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("할 일을 찾을 수 없습니다."));

        String calendarEventId = todo.getCalendarEventId();

        todoRepository.delete(todo);

        if (calendarEventId != null && !calendarEventId.isBlank()) {
            calendarAsyncService.deleteEventAsync(authentication, calendarEventId);
        }
    }

    private LocalDateTime parseDueDateTime(String dueDateTime) {
        if (dueDateTime == null || dueDateTime.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dueDateTime);
        } catch (DateTimeParseException e) {
            return OffsetDateTime.parse(dueDateTime)
                    .atZoneSameInstant(ZoneId.of("Asia/Seoul"))
                    .toLocalDateTime();
        }
    }
}