package back.pickd.application.dto.response;

import back.pickd.application.entity.Todo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TodoResponse {
    private Long id;
    private String title;
    private String memo;
    private Boolean completed;
    private LocalDateTime dueDateTime;
    private String calendarEventId;

    private Long applicationId;
    private String company;
    private String jobTitle;

    public static TodoResponse from(Todo todo) {
        return TodoResponse.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .memo(todo.getMemo())
                .completed(todo.isCompleted())
                .dueDateTime(todo.getDueDateTime())
                .calendarEventId(todo.getCalendarEventId())
                .applicationId(todo.getApplication() != null ? todo.getApplication().getId() : null)
                .company(todo.getApplication() != null ? todo.getApplication().getCompany() : null)
                .jobTitle(todo.getApplication() != null ? todo.getApplication().getJobTitle() : null)
                .build();
    }
}