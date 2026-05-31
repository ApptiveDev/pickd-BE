package back.pickd.user.controller;

import back.pickd.user.dto.ExperienceTempResponse;
import back.pickd.user.dto.UserExperienceResponse;
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
    public ResponseEntity<List<UserExperienceResponse>> extractStep2(
            Authentication authentication,
            @RequestBody Map<String, List<Long>> request) {

        List<Long> selectedTempIds = request.get("selectedTempIds");
        List<UserExperienceResponse> result = extractionService.extractStep2(authentication.getName(), selectedTempIds)
                .stream()
                .map(UserExperienceResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
