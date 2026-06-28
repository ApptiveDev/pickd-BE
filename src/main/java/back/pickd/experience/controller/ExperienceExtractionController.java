package back.pickd.experience.controller;

import back.pickd.experience.dto.ExperienceExtractionDto.Step2Response;
import back.pickd.experience.dto.ExperienceExtractionDto.Step2SaveResult;
import back.pickd.experience.dto.ExperienceExtractionDto.Step3Request;
import back.pickd.experience.dto.ExperienceExtractionDto.Step3Response;
import back.pickd.experience.dto.ExperienceExtractionDto.TempResponse;
import back.pickd.experience.dto.ExperienceResponse;
import back.pickd.experience.service.ExperienceExtractionService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 경험 추출 V1 컨트롤러.
 * step1(자소서 업로드 → 후보 추출)은 이 컨트롤러에만 존재하며 V2와 함께 운영됩니다.
 * step2/step3의 신규 개발은 V2({@link ExperienceExtractionV2Controller})를 사용하세요.
 */
@RestController
@RequestMapping("/api/experiences/extract")
@RequiredArgsConstructor
@Tag(name = "Experience Extraction V1", description = "자소서 파일 업로드 기반 경험 후보 추출 API (step1은 V1 전용)")
@SecurityRequirement(name = OpenApiConfig.COOKIE_AUTH)
public class ExperienceExtractionController {

    private final ExperienceExtractionService extractionService;

    @PostMapping(value = "/step1", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "경험 후보 1차 추출",
            description = """
                    자소서 파일을 업로드하면 AI가 경험 후보 목록을 추출하여 임시 저장합니다.

                    - 기존 임시 데이터는 초기화됩니다.
                    - 반환된 id를 step2의 selectedTempIds에 사용하세요.
                    - step2/step3는 V2 API(`/api/v2/experiences/extract`)를 사용하는 것을 권장합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경험 후보 추출 성공",
                    content = @Content(schema = @Schema(implementation = TempResponse.class))),
            @ApiResponse(responseCode = "400", description = "파일 누락 또는 AI 분석 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TempResponse>> extractStep1(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "자소서 PDF/문서 파일", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file) {

        List<TempResponse> result = extractionService.extractStep1(authentication.getName(), file)
                .stream()
                .map(TempResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/step2")
    @Operation(
            summary = "선택 경험 상세 추출 및 저장 (V1)",
            deprecated = true,
            description = """
                    step1에서 선택한 임시 경험 ID를 받아 AI 2차 분석 후 영구 저장합니다.

                    - 중복 경험은 mergeCandidates로 반환되며 step3에서 처리합니다.
                    - 신규 개발은 V2(`/api/v2/experiences/extract/step2`) 사용을 권장합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상세 추출 및 저장 성공",
                    content = @Content(schema = @Schema(implementation = Step2Response.class))),
            @ApiResponse(responseCode = "400", description = "선택된 임시 경험 ID 누락 또는 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Step2Response> extractStep2(
            @Parameter(hidden = true) Authentication authentication,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "step1에서 선택한 임시 경험 ID 목록",
                    content = @Content(schema = @Schema(example = "{\"selectedTempIds\": [1, 2, 3]}"))
            )
            @RequestBody Map<String, List<Long>> request) {

        List<Long> selectedTempIds = request.get("selectedTempIds");
        Step2SaveResult result = extractionService.extractStep2(authentication.getName(), selectedTempIds);
        List<ExperienceResponse> savedExperiences = result.getSavedExperiences()
                .stream()
                .map(ExperienceResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new Step2Response(savedExperiences, result.getMergeCandidates()));
    }

    @PostMapping("/step3")
    @Operation(
            summary = "중복 경험 후처리 (V1)",
            deprecated = true,
            description = """
                    step2에서 중복으로 분류된 경험을 CREATE_NEW 또는 SKIP 처리합니다.

                    - 신규 개발은 V2(`/api/v2/experiences/extract/step3`) 사용을 권장합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "후처리 성공",
                    content = @Content(schema = @Schema(implementation = Step3Response.class))),
            @ApiResponse(responseCode = "400", description = "요청 유효성 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Step3Response> confirmStep3(
            @Parameter(hidden = true) Authentication authentication,
            @RequestBody @Valid Step3Request request) {

        return ResponseEntity.ok(extractionService.confirmStep3(authentication.getName(), request));
    }

}
