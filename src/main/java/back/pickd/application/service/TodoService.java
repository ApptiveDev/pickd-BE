package back.pickd.application.service;

import back.pickd.application.dto.request.TodoRequest;
import back.pickd.application.dto.response.TodoResponse;
import back.pickd.application.entity.Application;
import back.pickd.application.entity.Todo;
import back.pickd.application.repository.ApplicationRepository;
import back.pickd.application.repository.TodoRepository;
import back.pickd.calendar.service.CalendarAsyncService;
import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
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
    private final UserService userService;

    public TodoResponse addTodo(TodoRequest dto, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());

        Application application = null;
        if (dto.getApplicationId() != null) {
            application = applicationRepository.findByIdAndUser(dto.getApplicationId(), user)
                    .orElseThrow(() -> new IllegalArgumentException("지원 공고를 찾을 수 없습니다."));
        }

        Todo todo = Todo.builder()
                .user(user)
                .application(application)
                .title(dto.getTitle())
                .memo(dto.getMemo())
                .dueDateTime(parseDueDateTime(dto.getDueDateTime()))
                .build();

        Todo saved = todoRepository.save(todo);

        if (saved.getDueDateTime() != null) {
            calendarAsyncService.createTodoEventAsync(saved.getId(), authentication);
        }

        return TodoResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> getTodos(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return todoRepository.findAllByUserWithApplication(user)
                .stream()
                .map(TodoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> getTodosByApplication(Long applicationId, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return todoRepository.findByApplicationIdAndUser(applicationId, user)
                .stream()
                .map(TodoResponse::from)
                .toList();
    }

    public void toggleTodo(Long id, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        Todo todo = todoRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("할 일을 찾을 수 없습니다."));
        todo.toggleCompleted();
    }

    public TodoResponse updateTodo(Long id, TodoRequest dto, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        Todo todo = todoRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("할 일을 찾을 수 없습니다."));
        todo.update(dto.getTitle(), parseDueDateTime(dto.getDueDateTime()), dto.getMemo());
        return TodoResponse.from(todo);
    }

    public void deleteTodo(Long id, Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        Todo todo = todoRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("할 일을 찾을 수 없습니다."));

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
