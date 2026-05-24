package back.pickd.application.dto.response;

import back.pickd.application.entity.Todo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoResponse {
    private Long id;
    private String title;
    private boolean completed;
    private String memo;
    private String dueDateTime;
    private Long applicationId;
    private String company;
    private String jobTitle;

    public static TodoResponse from(Todo todo) {
        return TodoResponse.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .completed(todo.isCompleted())
                .memo(todo.getMemo())
                .dueDateTime(
                        todo.getDueDateTime() != null
                                ? todo.getDueDateTime().toString()
                                : null
                )
                .applicationId(
                        todo.getApplication() != null
                                ? todo.getApplication().getId()
                                : null
                )
                .company(
                        todo.getApplication() != null
                                ? todo.getApplication().getCompany()
                                : null
                )
                .jobTitle(
                        todo.getApplication() != null
                                ? todo.getApplication().getJobTitle()
                                : null
                )
                .build();
    }
}