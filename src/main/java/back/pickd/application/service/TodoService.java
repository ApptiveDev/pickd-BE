package back.pickd.application.service;

import back.pickd.application.dto.request.TodoRequest;
import back.pickd.application.dto.response.TodoResponse;
import back.pickd.application.entity.Application;
import back.pickd.application.entity.Todo;
import back.pickd.application.repository.ApplicationRepository;
import back.pickd.application.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TodoService {
    private final TodoRepository todoRepository;
    private final ApplicationRepository applicationRepository;
    public TodoResponse addTodo(TodoRequest dto){
        Todo todo = new Todo();
        todo.setTitle(dto.getTitle());
        todo.setCompleted(false);
        todo.setMemo(dto.getMemo());

        if (
                dto.getDueDate() != null &&
                !dto.getDueDate().isBlank() &&
                dto.getDueTime() != null &&
                !dto.getDueTime().isBlank()
        ) {
            LocalDateTime dueDateTime = LocalDateTime.parse(
                    dto.getDueDate() + "T" + dto.getDueTime()
            );
            todo.setDueDateTime(dueDateTime);
        }
        if (dto.getApplicationId() != null) {
            Application application = applicationRepository
                    .findById(dto.getApplicationId())
                    .orElseThrow();

            todo.setApplication(application);
        }
        Todo saved = todoRepository.save(todo);
        return TodoResponse.from(saved);
    }

    public List<TodoResponse> getTodos() {
        return todoRepository.findAllWithApplication()
                .stream()
                .map(TodoResponse::from)
                .toList();
    }

    public List<TodoResponse> getTodosByApplication(Long applicationId) {
        return todoRepository.findByApplicationId(applicationId)
                .stream()
                .map(TodoResponse::from)
                .toList();
    }

    public void toggleTodo(Long id) {
        Todo todo = todoRepository.findById(id).orElseThrow();
        todo.setCompleted(!todo.isCompleted());
        todoRepository.save(todo);
    }

    public void deleteTodo(Long id) {
        todoRepository.deleteById(id);
    }
}
