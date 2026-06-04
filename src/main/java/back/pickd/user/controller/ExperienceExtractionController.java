package back.pickd.user.controller;

import back.pickd.user.dto.ExperienceTempResponse;
import back.pickd.user.dto.ExperienceStep3Request;
import back.pickd.user.dto.ExperienceStep3Response;
import back.pickd.user.dto.ExperienceStep2Response;
import back.pickd.user.dto.ExperienceStep2SaveResult;
import back.pickd.user.dto.UserExperienceResponse;
import back.pickd.global.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import back.pickd.user.service.ExperienceExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "경험 추출 (Experience Extraction)", description = "이력서/문서 기반 AI 경험 추출 및 STAR-L 중복 병합/저장 API")
@RestController
@RequestMapping("/api/experiences/extract")
@RequiredArgsConstructor
public class ExperienceExtractionController {

    private final ExperienceExtractionService extractionService;

    @Operation(
            summary = "이력서 기반 임시 경험 추출 (Step 1)",
            description = "업로드한 이력서/포트폴리오 파일을 S3 임시 영역에 저장하고, AI가 경험 후보 목록을 추출해 임시 경험 ID와 함께 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "임시 경험 후보 추출 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ExperienceTempResponse.class)))),
            @ApiResponse(responseCode = "400", description = "파일이 비어 있거나 요청이 올바르지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "파일 업로드 또는 AI 서버 연동 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/step1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ExperienceTempResponse>> extractStep1(
            Authentication authentication,
            @Parameter(description = "분석할 이력서/포트폴리오 파일", required = true)
            @RequestParam("file") MultipartFile file) {

        List<ExperienceTempResponse> result = extractionService.extractStep1(authentication.getName(), file)
                .stream()
                .map(ExperienceTempResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }


    @Operation(
            summary = "선택 경험 고도화 및 중복 확인 (Step 2)",
            description = "Step 1에서 반환된 임시 경험 ID 목록을 선택해 STAR-L 상세 구조로 고도화합니다. 중복이 없으면 즉시 저장하고, 중복 후보는 사용자 결정을 위해 보류 목록으로 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "선택 경험 고도화 및 중복 확인 성공",
                    content = @Content(schema = @Schema(implementation = ExperienceStep2Response.class))),
            @ApiResponse(responseCode = "400", description = "selectedTempIds가 없거나 요청값이 올바르지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "요청한 임시 경험을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "AI 서버 연동 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "고도화할 임시 경험 ID 목록",
            required = true,
            content = @Content(
                    schema = @Schema(type = "object", example = "{\"selectedTempIds\":[45,46,47]}"),
                    examples = @ExampleObject(value = "{\"selectedTempIds\":[45,46,47]}")
            )
    )
    @PostMapping("/step2")
    public ResponseEntity<ExperienceStep2Response> extractStep2(
            Authentication authentication,
            @RequestBody Map<String, List<Long>> request) {

        List<Long> selectedTempIds = request.get("selectedTempIds");
        ExperienceStep2SaveResult result = extractionService.extractStep2(authentication.getName(), selectedTempIds);
        List<UserExperienceResponse> savedExperiences = result.getSavedExperiences()
                .stream()
                .map(UserExperienceResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ExperienceStep2Response(savedExperiences, result.getMergeCandidates()));
    }

    @Operation(
            summary = "중복 후보 처리 최종 결정 (Step 3)",
            description = "Step 2에서 보류된 중복 후보에 대해 사용자가 CREATE_NEW 또는 SKIP 결정을 내려 최종 저장 여부를 확정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "중복 후보 결정 처리 성공",
                    content = @Content(schema = @Schema(implementation = ExperienceStep3Response.class))),
            @ApiResponse(responseCode = "400", description = "결정 목록 또는 draft 데이터가 올바르지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "보류된 중복 후보별 사용자 결정 목록",
            required = true,
            content = @Content(schema = @Schema(implementation = ExperienceStep3Request.class))
    )
    @PostMapping("/step3")
    public ResponseEntity<ExperienceStep3Response> confirmStep3(
            Authentication authentication,
            @RequestBody @Valid ExperienceStep3Request request) {

        return ResponseEntity.ok(extractionService.confirmStep3(authentication.getName(), request));
    }

}
