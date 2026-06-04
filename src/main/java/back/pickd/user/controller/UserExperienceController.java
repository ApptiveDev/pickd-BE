package back.pickd.user.controller;

import back.pickd.user.dto.ExperienceCreateRequestDto;
import back.pickd.user.dto.ExperienceCreateResponseDto;
import back.pickd.user.dto.ExperienceMergeConflictResponse;
import back.pickd.user.dto.UserExperienceResponse;
import back.pickd.global.error.ErrorResponse;
import back.pickd.user.entity.enums.ExperienceGroup;
import back.pickd.user.entity.enums.ExperienceType;
import back.pickd.user.service.UserExperienceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 사용자 경험 CR 컨트롤러
@Tag(name = "경험 관리 (Experiences)", description = "사용자가 직접 입력한 경험 카드 생성 및 조회 API")
@RestController
@RequestMapping("/api/experiences")
@RequiredArgsConstructor
public class UserExperienceController {

    private final UserExperienceService userExperienceService;

    // 경험 수기 생성
    @Operation(
            summary = "경험 수기 생성",
            description = "현재 로그인한 사용자의 경험 카드를 생성합니다. forceCreate가 false이면 기존 경험과 유사한 후보가 있을 때 409 응답으로 중복 후보를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경험 생성 성공",
                    content = @Content(schema = @Schema(implementation = ExperienceCreateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "요청값이 올바르지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "기존 경험과 중복 가능성이 있어 사용자 확인이 필요함",
                    content = @Content(schema = @Schema(implementation = ExperienceMergeConflictResponse.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "생성할 경험 카드 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = ExperienceCreateRequestDto.class))
    )
    @PostMapping
    public ResponseEntity<ExperienceCreateResponseDto> createExperience(
            Authentication authentication,
            @RequestBody @Valid ExperienceCreateRequestDto request) {
        return ResponseEntity.ok(
                userExperienceService.createExperience(authentication.getName(), request));
    }

    // 경험 단일 조회
    @Operation(
            summary = "경험 단일 조회",
            description = "경험 ID를 기준으로 현재 로그인한 사용자의 경험 카드 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경험 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserExperienceResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 또는 경험을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserExperienceResponse> getExperience(
            Authentication authentication,
            @Parameter(description = "조회할 경험 ID", example = "exp_7f8a9b2c")
            @PathVariable String id) {
        return ResponseEntity.ok(
                userExperienceService.getExperience(authentication.getName(), id));
    }

    // 경험 목록 조회 (필터링 적용)
    @Operation(
            summary = "경험 목록 조회",
            description = "현재 로그인한 사용자의 경험 목록을 조회합니다. 경험 유형(type)과 그룹(group)을 선택적으로 필터링할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경험 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserExperienceResponse.class)))),
            @ApiResponse(responseCode = "400", description = "필터 값이 올바르지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<UserExperienceResponse>> getExperiences(
            Authentication authentication,
            @Parameter(description = "경험 유형 필터", example = "PROJECT")
            @RequestParam(required = false) ExperienceType type,
            @Parameter(description = "경험 그룹 필터", example = "NARRATIVE")
            @RequestParam(required = false) ExperienceGroup group) {
        return ResponseEntity.ok(
                userExperienceService.getExperiences(authentication.getName(), type, group));
    }
}
