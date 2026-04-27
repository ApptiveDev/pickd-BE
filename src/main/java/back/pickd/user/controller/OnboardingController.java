package back.pickd.user.controller;

import back.pickd.user.dto.onboarding.*;
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

    @GetMapping("/status")
    public ResponseEntity<String> getStatus(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(user.getOnboardingStep().name());
    }

    @PostMapping("/reset")
    public ResponseEntity<String> reset(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        onboardingService.resetOnboarding(user);
        return ResponseEntity.ok("Onboarding reset successfully");
    }

    @PostMapping("/step1")
    public ResponseEntity<String> saveStep1(Authentication authentication, @Valid @RequestBody Step1TermsRequest request) {
        User user = userService.findByEmail(authentication.getName());
        onboardingService.saveStep1Terms(user, request);
        return ResponseEntity.ok("Step 1 saved");
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(Authentication authentication, @Valid @RequestBody StepVerificationRequest request) {
        User user = userService.findByEmail(authentication.getName());
        onboardingService.saveVerification(user, request);
        return ResponseEntity.ok("Verification completed");
    }

    @PostMapping("/step2")
    public ResponseEntity<String> saveStep2(Authentication authentication, @Valid @RequestBody Step2BasicInfoRequest request) {
        User user = userService.findByEmail(authentication.getName());
        onboardingService.saveStep2BasicInfo(user, request);
        return ResponseEntity.ok("Step 2 saved");
    }

    @PostMapping("/step3")
    public ResponseEntity<String> saveStep3(Authentication authentication, @Valid @RequestBody Step3EducationRequest request) {
        User user = userService.findByEmail(authentication.getName());
        onboardingService.saveStep3Education(user, request);
        return ResponseEntity.ok("Step 3 saved");
    }

    @PostMapping("/step4")
    public ResponseEntity<String> saveStep4(Authentication authentication, @Valid @RequestBody Step4InterestRequest request) {
        User user = userService.findByEmail(authentication.getName());
        onboardingService.saveStep4Interest(user, request);
        return ResponseEntity.ok("Step 4 saved");
    }

    @PostMapping("/step5")
    public ResponseEntity<String> saveStep5(Authentication authentication, @Valid @RequestBody Step5PrepStatusRequest request) {
        User user = userService.findByEmail(authentication.getName());
        onboardingService.saveStep5PrepStatus(user, request);
        return ResponseEntity.ok("Onboarding completed");
    }
}
