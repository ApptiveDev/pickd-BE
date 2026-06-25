package back.pickd.application.controller;

import back.pickd.application.dto.request.ApplicationRequest;
import back.pickd.application.dto.response.ApplicationResponse;
import back.pickd.application.service.ApplicationService;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/application")
@Tag(name = "Application", description = "지원 공고 관리 API")
@SecurityRequirement(name = OpenApiConfig.COOKIE_AUTH)
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping
    @Operation(summary = "지원 공고 목록 조회", description = "로그인 사용자의 전체 지원 공고를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<ApplicationResponse> getAll(@Parameter(hidden = true) Authentication auth) {
        return applicationService.getApplications(auth);
    }

    @PostMapping
    @Operation(summary = "지원 공고 추가", description = "새 지원 공고를 등록합니다. noticeId가 있으면 채용공고와 연결됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추가 성공"),
            @ApiResponse(responseCode = "400", description = "요청 유효성 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void add(@RequestBody @Valid ApplicationRequest dto, @Parameter(hidden = true) Authentication auth) throws Exception {
        applicationService.addApplication(dto, auth);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "지원 공고 삭제", description = "본인 소유의 지원 공고를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "소유권 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void delete(@PathVariable Long id, @Parameter(hidden = true) Authentication auth) throws Exception {
        applicationService.deleteApplication(id, auth);
    }

    @PutMapping("/{id}")
    @Operation(summary = "지원 공고 수정", description = "지원 공고 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "소유권 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void update(@PathVariable Long id,
                       @RequestBody @Valid ApplicationRequest dto,
                       @Parameter(hidden = true) Authentication auth) throws Exception {
        applicationService.updateApplication(id, dto, auth);
    }
}
