package back.pickd.user.controller;

import back.pickd.user.dto.ExperienceTempResponse;
import back.pickd.user.dto.ExperienceStep3Request;
import back.pickd.user.dto.ExperienceStep3Response;
import back.pickd.user.dto.ExperienceStep2Response;
import back.pickd.user.dto.ExperienceStep2SaveResult;
import back.pickd.user.dto.UserExperienceResponse;
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

@RestController
@RequestMapping("/api/experiences/extract")
@RequiredArgsConstructor
public class ExperienceExtractionController {

    private final ExperienceExtractionService extractionService;

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

    @PostMapping("/step3")
    public ResponseEntity<ExperienceStep3Response> confirmStep3(
            Authentication authentication,
            @RequestBody @Valid ExperienceStep3Request request) {

        return ResponseEntity.ok(extractionService.confirmStep3(authentication.getName(), request));
    }

}
