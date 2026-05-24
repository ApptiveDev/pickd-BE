package back.pickd.application.controller;

import back.pickd.application.dto.request.TodoRequest;
import back.pickd.application.dto.response.TodoResponse;
import back.pickd.application.entity.Todo;
import back.pickd.application.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todo")
@RequiredArgsConstructor
public class TodoController {
    private final TodoService todoService;

    @PostMapping
    public TodoResponse addTodo(@RequestBody TodoRequest dto) {
        return todoService.addTodo(dto);
    }

    @GetMapping
    public List<TodoResponse> getTodos() {
        return todoService.getTodos();
    }

    @GetMapping("/{applicationId}")
    public List<TodoResponse> getTodosByApplication(
            @PathVariable Long applicationId
    ) {
        return todoService.getTodosByApplication(applicationId);
    }

    @PutMapping("/{id}")
    public void toggleTodo(@PathVariable Long id) {
        todoService.toggleTodo(id);
    }

    @DeleteMapping("/{id}")
    public void deleteTodo(@PathVariable Long id) {
        todoService.deleteTodo(id);
    }
}