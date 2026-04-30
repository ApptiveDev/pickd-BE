package back.pickd.user.controller;

import back.pickd.user.dto.UserResponseDto;
import back.pickd.user.dto.onboarding.OnboardingRequest;
import back.pickd.user.entity.User;
import back.pickd.user.service.OnboardingService;
import back.pickd.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> updateOnboarding(Authentication authentication, @RequestBody @Valid OnboardingRequest request) {
        User user = onboardingService.updateOnboarding(authentication.getName(), request);
        return ResponseEntity.ok(UserResponseDto.from(user));
    }

    @GetMapping("/status")
    public ResponseEntity<UserResponseDto> getStatus(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(UserResponseDto.from(user));
    }

    @PostMapping("/reset")
    public ResponseEntity<String> reset(Authentication authentication) {
        onboardingService.resetOnboarding(authentication.getName());
        return ResponseEntity.ok("Reset complete");
    }
}
