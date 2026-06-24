package back.pickd.application.controller;

import back.pickd.application.dto.request.TodoRequest;
import back.pickd.application.dto.response.TodoResponse;
import back.pickd.application.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todo")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @PostMapping
    public TodoResponse addTodo(@RequestBody TodoRequest dto, Authentication authentication) {
        return todoService.addTodo(dto, authentication);
    }

    @GetMapping
    public List<TodoResponse> getTodos(Authentication authentication) {
        return todoService.getTodos(authentication);
    }

    @GetMapping("/application/{applicationId}")
    public List<TodoResponse> getTodosByApplication(@PathVariable Long applicationId,
                                                     Authentication authentication) {
        return todoService.getTodosByApplication(applicationId, authentication);
    }

    @PutMapping("/{id}")
    public void toggleTodo(@PathVariable Long id, Authentication authentication) {
        todoService.toggleTodo(id, authentication);
    }

    @DeleteMapping("/{id}")
    public void deleteTodo(@PathVariable Long id, Authentication authentication) {
        todoService.deleteTodo(id, authentication);
    }
}
