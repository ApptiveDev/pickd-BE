package back.pickd.experience.controller;

import back.pickd.experience.dto.ExperienceCreateDto.Request;
import back.pickd.experience.dto.ExperienceCreateDto.Response;
import back.pickd.experience.dto.ExperienceResponse;
import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.experience.service.UserExperienceService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/experiences")
@RequiredArgsConstructor
@Tag(name = "UserExperience", description = "사용자 경험 CRUD API")
@SecurityRequirement(name = OpenApiConfig.COOKIE_AUTH)
public class UserExperienceController {

    private final UserExperienceService userExperienceService;

    @PostMapping
    @Operation(summary = "경험 수기 생성", description = "경험을 직접 입력하여 생성합니다. AI 추출 없이 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "요청 유효성 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Response> createExperience(
            @Parameter(hidden = true) Authentication authentication,
            @RequestBody @Valid Request request) {
        return ResponseEntity.ok(
                userExperienceService.createExperience(authentication.getName(), request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "경험 단일 조회", description = "경험 ID로 단일 경험을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ExperienceResponse.class))),
            @ApiResponse(responseCode = "404", description = "경험 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ExperienceResponse> getExperience(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable String id) {
        return ResponseEntity.ok(
                userExperienceService.getExperience(authentication.getName(), id));
    }

    @GetMapping
    @Operation(
            summary = "경험 목록 조회",
            description = "type, group 쿼리 파라미터로 필터링합니다. 둘 다 없으면 전체를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ExperienceResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ExperienceResponse>> getExperiences(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "경험 유형 (PROJECT, INTERN 등)") @RequestParam(required = false) ExperienceType type,
            @Parameter(description = "경험 대분류 (NARRATIVE / SPEC)") @RequestParam(required = false) ExperienceGroup group) {
        return ResponseEntity.ok(
                userExperienceService.getExperiences(authentication.getName(), type, group));
    }

    @PutMapping("/{id}")
    @Operation(summary = "경험 수정", description = "경험 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = ExperienceResponse.class))),
            @ApiResponse(responseCode = "404", description = "경험 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ExperienceResponse> updateExperience(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable String id,
            @RequestBody @Valid Request request) {
        return ResponseEntity.ok(
                userExperienceService.updateExperience(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "경험 삭제", description = "경험을 삭제합니다. 204 No Content를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "경험 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteExperience(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable String id) {
        userExperienceService.deleteExperience(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
