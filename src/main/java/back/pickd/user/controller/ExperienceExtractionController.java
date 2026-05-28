package back.pickd.user.controller;

import back.pickd.user.dto.ExperienceTempResponse;
import back.pickd.user.dto.UserExperienceResponse;
import back.pickd.user.entity.User;
import back.pickd.user.service.ExperienceExtractionService;
import back.pickd.user.service.UserService;
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
    private final UserService userService;
    private final back.pickd.auth.jwt.JwtTokenProvider jwtTokenProvider;

    /**
     * 1차 경험 후보 추출 API (자소서 업로드 ➔ 후보 반환)
     */
    @PostMapping("/step1")
    public ResponseEntity<List<ExperienceTempResponse>> extractStep1(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {

        User user = userService.findByEmail(authentication.getName());
        List<ExperienceTempResponse> result = extractionService.extractStep1(user, file)
                .stream()
                .map(ExperienceTempResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * 2차 정밀 추출 및 DB 최종 저장 API (선택 확정 ➔ 영구 저장)
     */
    @PostMapping("/step2")
    public ResponseEntity<List<UserExperienceResponse>> extractStep2(
            Authentication authentication,
            @RequestBody Map<String, List<Long>> request) {

        User user = userService.findByEmail(authentication.getName());
        List<Long> selectedTempIds = request.get("selectedTempIds");
        List<UserExperienceResponse> result = extractionService.extractStep2(user, selectedTempIds)
                .stream()
                .map(UserExperienceResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * 로컬 테스트용 임시 토큰 발급 API
     */
    @GetMapping("/temp-token")
    public ResponseEntity<Map<String, String>> getTempToken() {
        userService.saveOrUpdate("test@gmail.com", "테스트유저", "https://example.com/test.jpg");
        String token = jwtTokenProvider.createToken("test@gmail.com",
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));
        return ResponseEntity.ok(Map.of("token", token));
    }
}
