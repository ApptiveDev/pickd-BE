package back.pickd.experience.controller;

import back.pickd.experience.dto.ExperienceCreateDto.Request;
import back.pickd.experience.dto.ExperienceCreateDto.Response;
import back.pickd.experience.dto.ExperienceResponse;
import back.pickd.experience.enums.ExperienceGroup;
import back.pickd.experience.enums.ExperienceType;
import back.pickd.experience.service.UserExperienceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 사용자 경험 CR 컨트롤러
@RestController
@RequestMapping("/api/experiences")
@RequiredArgsConstructor
public class UserExperienceController {

    private final UserExperienceService userExperienceService;

    // 경험 수기 생성
    @PostMapping
    public ResponseEntity<Response> createExperience(
            Authentication authentication,
            @RequestBody @Valid Request request) {
        return ResponseEntity.ok(
                userExperienceService.createExperience(authentication.getName(), request));
    }

    // 경험 단일 조회
    @GetMapping("/{id}")
    public ResponseEntity<ExperienceResponse> getExperience(
            Authentication authentication,
            @PathVariable String id) {
        return ResponseEntity.ok(
                userExperienceService.getExperience(authentication.getName(), id));
    }

    // 경험 목록 조회 (필터링 적용)
    @GetMapping
    public ResponseEntity<List<ExperienceResponse>> getExperiences(
            Authentication authentication,
            @RequestParam(required = false) ExperienceType type,
            @RequestParam(required = false) ExperienceGroup group) {
        return ResponseEntity.ok(
                userExperienceService.getExperiences(authentication.getName(), type, group));
    }
}
