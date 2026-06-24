package back.pickd.application.service;

import back.pickd.application.dto.request.TodoRequest;
import back.pickd.application.dto.response.TodoResponse;
import back.pickd.application.entity.Application;
import back.pickd.application.entity.Todo;
import back.pickd.application.enums.ApplicationStatus;
import back.pickd.application.repository.ApplicationRepository;
import back.pickd.application.repository.TodoRepository;
import back.pickd.calendar.service.CalendarAsyncService;
import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TodoService 단위 테스트")
class TodoServiceTest {

    @Mock TodoRepository todoRepository;
    @Mock ApplicationRepository applicationRepository;
    @Mock CalendarAsyncService calendarAsyncService;
    @Mock UserService userService;
    @Mock Authentication authentication;

    @InjectMocks TodoService todoService;

    User user;
    Application application;

    @BeforeEach
    void setUp() {
        user = User.builder().email("test@test.com").name("테스터").build();
        application = Application.builder()
                .user(user).company("카카오").jobTitle("백엔드")
                .status(ApplicationStatus.WRITING).important(false).build();

        when(authentication.getName()).thenReturn("test@test.com");
        when(userService.findByEmail("test@test.com")).thenReturn(user);
    }

    private Todo buildTodo(boolean withCalendarEvent) {
        Todo todo = Todo.builder()
                .user(user)
                .application(application)
                .title("자소서 작성")
                .memo("1번 문항")
                .build();
        if (withCalendarEvent) {
            todo.assignCalendarEventId("event-id-abc");
        }
        return todo;
    }

    // ── addTodo ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addTodo")
    class AddTodo {

        @Test
        @DisplayName("applicationId 없이 독립 Todo를 생성할 수 있다")
        void createsStandaloneTodo() {
            TodoRequest req = new TodoRequest();
            req.setTitle("독립 할 일");
            // applicationId = null
            when(todoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TodoResponse response = todoService.addTodo(req, authentication);

            ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
            verify(todoRepository).save(captor.capture());
            assertThat(captor.getValue().getApplication()).isNull();
            assertThat(captor.getValue().getUser()).isEqualTo(user);
        }

        @Test
        @DisplayName("applicationId가 있으면 Application 소유권을 검증한 후 Todo를 생성한다")
        void createsTodoLinkedToApplication() {
            TodoRequest req = new TodoRequest();
            req.setTitle("공고 연결 할 일");
            req.setApplicationId(1L);
            when(applicationRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(application));
            when(todoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            todoService.addTodo(req, authentication);

            ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
            verify(todoRepository).save(captor.capture());
            assertThat(captor.getValue().getApplication()).isEqualTo(application);
        }

        @Test
        @DisplayName("다른 사용자의 Application에 Todo 생성 시 예외가 발생한다")
        void throwsWhenApplicationNotOwned() {
            TodoRequest req = new TodoRequest();
            req.setTitle("할 일");
            req.setApplicationId(99L);
            when(applicationRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> todoService.addTodo(req, authentication))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("지원 공고를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("dueDateTime이 있으면 캘린더 이벤트를 생성한다")
        void createsCalendarEventWhenDueDateTimeExists() {
            TodoRequest req = new TodoRequest();
            req.setTitle("마감 있는 할 일");
            req.setDueDateTime("2026-12-01T09:00:00");
            when(todoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            todoService.addTodo(req, authentication);

            verify(calendarAsyncService).createTodoEventAsync(any(), eq(authentication));
        }

        @Test
        @DisplayName("dueDateTime이 없으면 캘린더 이벤트를 생성하지 않는다")
        void doesNotCreateCalendarEventWhenNoDueDateTime() {
            TodoRequest req = new TodoRequest();
            req.setTitle("마감 없는 할 일");
            when(todoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            todoService.addTodo(req, authentication);

            verify(calendarAsyncService, never()).createTodoEventAsync(any(), any());
        }

        @Test
        @DisplayName("ISO offset 형식의 dueDateTime도 파싱된다")
        void parsesOffsetDateTimeFormat() {
            TodoRequest req = new TodoRequest();
            req.setTitle("오프셋 날짜 할 일");
            req.setDueDateTime("2026-12-01T09:00:00+09:00");
            when(todoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertThatCode(() -> todoService.addTodo(req, authentication))
                    .doesNotThrowAnyException();
        }
    }

    // ── getTodos ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getTodos")
    class GetTodos {

        @Test
        @DisplayName("사용자의 전체 Todo 목록을 반환한다")
        void returnsAllTodosForUser() {
            when(todoRepository.findAllByUserWithApplication(user))
                    .thenReturn(List.of(buildTodo(false), buildTodo(false)));

            List<TodoResponse> result = todoService.getTodos(authentication);

            assertThat(result).hasSize(2);
            verify(todoRepository).findAllByUserWithApplication(user);
        }
    }

    // ── getTodosByApplication ──────────────────────────────────────────────────

    @Nested
    @DisplayName("getTodosByApplication")
    class GetTodosByApplication {

        @Test
        @DisplayName("특정 공고에 속한 Todo 목록을 반환한다")
        void returnsTodosForApplication() {
            when(todoRepository.findByApplicationIdAndUser(1L, user))
                    .thenReturn(List.of(buildTodo(false)));

            List<TodoResponse> result = todoService.getTodosByApplication(1L, authentication);

            assertThat(result).hasSize(1);
            verify(todoRepository).findByApplicationIdAndUser(1L, user);
        }
    }

    // ── toggleTodo ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("toggleTodo")
    class ToggleTodo {

        @Test
        @DisplayName("completed가 false인 Todo를 toggle하면 true가 된다")
        void togglesCompletedFromFalseToTrue() {
            Todo todo = buildTodo(false);
            assertThat(todo.isCompleted()).isFalse();
            when(todoRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(todo));

            todoService.toggleTodo(1L, authentication);

            assertThat(todo.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("completed가 true인 Todo를 toggle하면 false가 된다")
        void togglesCompletedFromTrueToFalse() {
            Todo todo = buildTodo(false);
            todo.toggleCompleted(); // true로 만들기
            when(todoRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(todo));

            todoService.toggleTodo(1L, authentication);

            assertThat(todo.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("다른 사용자의 Todo toggle 시 예외가 발생한다")
        void throwsWhenTogglingOtherUsersTodo() {
            when(todoRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> todoService.toggleTodo(99L, authentication))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("할 일을 찾을 수 없습니다");
        }
    }

    // ── deleteTodo ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteTodo")
    class DeleteTodo {

        @Test
        @DisplayName("소유자가 맞으면 Todo를 삭제한다")
        void deletesTodoForOwner() {
            Todo todo = buildTodo(false);
            when(todoRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(todo));

            todoService.deleteTodo(1L, authentication);

            verify(todoRepository).delete(todo);
        }

        @Test
        @DisplayName("calendarEventId가 있으면 캘린더 이벤트를 삭제한다")
        void deletesCalendarEventWhenEventIdExists() {
            Todo todo = buildTodo(true); // calendarEventId = "event-id-abc"
            when(todoRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(todo));

            todoService.deleteTodo(1L, authentication);

            verify(calendarAsyncService).deleteEventAsync(authentication, "event-id-abc");
        }

        @Test
        @DisplayName("calendarEventId가 없으면 캘린더 삭제를 호출하지 않는다")
        void doesNotDeleteCalendarEventWhenNoEventId() {
            Todo todo = buildTodo(false);
            when(todoRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(todo));

            todoService.deleteTodo(1L, authentication);

            verify(calendarAsyncService, never()).deleteEventAsync(any(), any());
        }

        @Test
        @DisplayName("다른 사용자의 Todo 삭제 시 예외가 발생한다")
        void throwsWhenDeletingOtherUsersTodo() {
            when(todoRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> todoService.deleteTodo(99L, authentication))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("할 일을 찾을 수 없습니다");
        }
    }
}
