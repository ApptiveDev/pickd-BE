package back.pickd.user.controller;

import back.pickd.user.dto.ExperienceTempResponse;
import back.pickd.user.dto.ExperienceStep3Request;
import back.pickd.user.dto.ExperienceStep3Response;
import back.pickd.user.dto.ExperienceStep2Response;
import back.pickd.user.dto.ExperienceStep2SaveResult;
import back.pickd.user.dto.UserExperienceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import back.pickd.user.service.ExperienceExtractionService;
import lombok.RequiredArgsConstructor;
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

    @Operation(summary = "이력서 기반 임시 경험 추출 (Step 1)", description = "업로드한 이력서/포트폴리오 파일에서 AI가 경험 후보 목록(임시 ID)을 일차적으로 추출합니다.")
    @PostMapping("/step1")
    public ResponseEntity<List<ExperienceTempResponse>> extractStep1(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {

        List<ExperienceTempResponse> result = extractionService.extractStep1(authentication.getName(), file)
                .stream()
                .map(ExperienceTempResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }


    @Operation(summary = "선택 경험 고도화 및 중복 확인 (Step 2)", description = "선택한 임시 경험들을 STAR-L 상세 구조로 추출하고, 기존 경험들과 비교하여 중복 가능성을 검사합니다.")
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

    @Operation(summary = "중복 후보 처리 최종 결정 (Step 3)", description = "중복으로 판단된 경험 후보들에 대해 사용자가 '신규 생성(CREATE_NEW)' 또는 '패스(SKIP)' 결정을 내려 최종 저장합니다.")
    @PostMapping("/step3")
    public ResponseEntity<ExperienceStep3Response> confirmStep3(
            Authentication authentication,
            @RequestBody @Valid ExperienceStep3Request request) {

        return ResponseEntity.ok(extractionService.confirmStep3(authentication.getName(), request));
    }

}
