package back.pickd.experience.controller;

import back.pickd.experience.dto.ExperienceExtractionDto.Step2Response;
import back.pickd.experience.dto.ExperienceExtractionDto.Step2SaveResult;
import back.pickd.experience.dto.ExperienceExtractionDto.Step3Request;
import back.pickd.experience.dto.ExperienceExtractionDto.Step3Response;
import back.pickd.experience.dto.ExperienceExtractionDto.TempResponse;
import back.pickd.experience.dto.ExperienceResponse;
import back.pickd.experience.service.ExperienceExtractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/experiences/extract")
@RequiredArgsConstructor
public class ExperienceExtractionController {

    private final ExperienceExtractionService extractionService;

    @PostMapping("/step1")
    public ResponseEntity<List<TempResponse>> extractStep1(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {

        List<TempResponse> result = extractionService.extractStep1(authentication.getName(), file)
                .stream()
                .map(TempResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }


    @PostMapping("/step2")
    public ResponseEntity<Step2Response> extractStep2(
            Authentication authentication,
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
    public ResponseEntity<Step3Response> confirmStep3(
            Authentication authentication,
            @RequestBody @Valid Step3Request request) {

        return ResponseEntity.ok(extractionService.confirmStep3(authentication.getName(), request));
    }

}
