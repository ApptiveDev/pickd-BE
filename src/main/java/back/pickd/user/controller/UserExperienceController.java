package back.pickd.user.controller;

import back.pickd.user.dto.ExperienceCreateRequestDto;
import back.pickd.user.dto.ExperienceCreateResponseDto;
import back.pickd.user.dto.UserExperienceResponse;
import back.pickd.user.entity.enums.ExperienceGroup;
import back.pickd.user.entity.enums.ExperienceType;
import back.pickd.user.service.UserExperienceService;
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
    public ResponseEntity<ExperienceCreateResponseDto> createExperience(
            Authentication authentication,
            @RequestBody @Valid ExperienceCreateRequestDto request) {
        return ResponseEntity.ok(
                userExperienceService.createExperience(authentication.getName(), request));
    }

    // 경험 단일 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserExperienceResponse> getExperience(
            Authentication authentication,
            @PathVariable String id) {
        return ResponseEntity.ok(
                userExperienceService.getExperience(authentication.getName(), id));
    }

    // 경험 목록 조회 (필터링 적용)
    @GetMapping
    public ResponseEntity<List<UserExperienceResponse>> getExperiences(
            Authentication authentication,
            @RequestParam(required = false) ExperienceType type,
            @RequestParam(required = false) ExperienceGroup group) {
        return ResponseEntity.ok(
                userExperienceService.getExperiences(authentication.getName(), type, group));
    }
}
