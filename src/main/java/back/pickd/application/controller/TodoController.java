package back.pickd.application.controller;

import back.pickd.application.dto.request.TodoRequest;
import back.pickd.application.dto.response.TodoResponse;
import back.pickd.application.service.TodoService;
import back.pickd.global.config.OpenApiConfig;
import back.pickd.global.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todo")
@RequiredArgsConstructor
@Tag(name = "Todo", description = "지원 공고별 할 일(Todo) 관리 API")
@SecurityRequirement(name = OpenApiConfig.COOKIE_AUTH)
public class TodoController {

    private final TodoService todoService;

    @PostMapping
    @Operation(summary = "할 일 추가", description = "지원 공고에 할 일을 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추가 성공",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TodoResponse addTodo(@RequestBody TodoRequest dto, @Parameter(hidden = true) Authentication authentication) {
        return todoService.addTodo(dto, authentication);
    }

    @GetMapping
    @Operation(summary = "전체 할 일 조회", description = "로그인 사용자의 모든 할 일을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<TodoResponse> getTodos(@Parameter(hidden = true) Authentication authentication) {
        return todoService.getTodos(authentication);
    }

    @GetMapping("/application/{applicationId}")
    @Operation(summary = "공고별 할 일 조회", description = "특정 지원 공고의 할 일 목록을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<TodoResponse> getTodosByApplication(@PathVariable Long applicationId,
                                                     @Parameter(hidden = true) Authentication authentication) {
        return todoService.getTodosByApplication(applicationId, authentication);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "할 일 수정", description = "할 일의 title, dueDateTime, memo를 부분 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TodoResponse updateTodo(@PathVariable Long id,
                                   @RequestBody TodoRequest dto,
                                   @Parameter(hidden = true) Authentication authentication) {
        return todoService.updateTodo(id, dto, authentication);
    }

    @PutMapping("/{id}/toggle")
    @Operation(summary = "할 일 완료 토글", description = "할 일의 완료 상태를 반전시킵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void toggleTodo(@PathVariable Long id, @Parameter(hidden = true) Authentication authentication) {
        todoService.toggleTodo(id, authentication);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "할 일 삭제", description = "할 일을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void deleteTodo(@PathVariable Long id, @Parameter(hidden = true) Authentication authentication) {
        todoService.deleteTodo(id, authentication);
    }
}
