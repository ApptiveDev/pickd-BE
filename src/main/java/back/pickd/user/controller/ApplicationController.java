package back.pickd.user.controller;

import back.pickd.user.dto.ApplicationRequest;
import back.pickd.user.entity.Application;
import back.pickd.user.service.ApplicationService;
import back.pickd.user.repository.ApplicationRepository;
import back.pickd.global.error.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@Tag(name = "지원 관리 (Applications)", description = "지원 현황 CRUD 및 지원/마감 일정 Google Calendar 연동 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/application")
public class ApplicationController {
    private final ApplicationService applicationService;
    private final ApplicationRepository applicationRepository;

    @Operation(
            summary = "지원 현황 목록 조회",
            description = "저장된 지원 현황을 ID 내림차순으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "지원 현황 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Application.class)))),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public List<Application> getAll() {
        return applicationRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    @Operation(
            summary = "지원 현황 등록",
            description = "지원 현황을 저장합니다. applyDate 또는 deadlineDate가 있으면 각각 Google Calendar에 제출/마감 일정을 생성하고 이벤트 ID를 함께 저장합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "지원 현황 등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청값이 올바르지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Google Calendar 연동 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "등록할 지원 현황 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = ApplicationRequest.class))
    )
    @PostMapping
    public void add(@RequestBody ApplicationRequest dto, Authentication auth) throws Exception {
        applicationService.addApplication(dto, auth);
    }

    @Operation(
            summary = "지원 현황 삭제",
            description = "지원 현황을 삭제합니다. 연결된 Google Calendar 제출/마감 일정이 있으면 함께 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "지원 현황 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "지원 현황을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Google Calendar 연동 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public void delete(
            @Parameter(description = "삭제할 지원 현황 ID", example = "1")
            @PathVariable Long id,
            Authentication auth) throws Exception {
        applicationService.deleteApplication(id, auth);
    }

    @Operation(
            summary = "지원 현황 수정",
            description = "지원 현황을 수정합니다. applyDate 또는 deadlineDate 변경에 따라 Google Calendar 제출/마감 일정을 생성, 수정 또는 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "지원 현황 수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값이 올바르지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "지원 현황을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Google Calendar 연동 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "수정할 지원 현황 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = ApplicationRequest.class))
    )
    @PutMapping("/{id}")
    public void update(@Parameter(description = "수정할 지원 현황 ID", example = "1")
                       @PathVariable Long id,
                       @RequestBody ApplicationRequest dto,
                       Authentication auth) throws Exception {
        applicationService.updateApplication(id, dto, auth);
    }
}
