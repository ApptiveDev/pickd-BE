package back.pickd.user.controller;

import back.pickd.global.config.OpenApiConfig;
import back.pickd.global.error.ErrorResponse;
import back.pickd.user.dto.UserResponseDto;
import back.pickd.user.dto.onboarding.OnboardingRequest;
import back.pickd.user.entity.User;
import back.pickd.user.service.OnboardingService;
import back.pickd.user.service.UserService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
@Tag(name = "Onboarding", description = "회원 온보딩 단계 관리 API")
@SecurityRequirement(name = OpenApiConfig.COOKIE_AUTH)
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final UserService userService;

    @PostMapping
    @Operation(
            summary = "온보딩 정보 저장",
            description = "단계별 온보딩 정보를 저장합니다. 요청 본문에 해당 단계 필드만 포함하면 됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "요청 유효성 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponseDto> updateOnboarding(
            @Parameter(hidden = true) Authentication authentication,
            @RequestBody @Valid OnboardingRequest request) {
        User user = onboardingService.updateOnboarding(authentication.getName(), request);
        return ResponseEntity.ok(UserResponseDto.from(user));
    }

    @GetMapping("/status")
    @Operation(summary = "온보딩 상태 조회", description = "현재 온보딩 단계와 사용자 기본 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponseDto> getStatus(@Parameter(hidden = true) Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(UserResponseDto.from(user));
    }

    @PostMapping("/reset")
    @Operation(summary = "온보딩 초기화", description = "온보딩 단계를 처음으로 되돌립니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "초기화 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> reset(@Parameter(hidden = true) Authentication authentication) {
        onboardingService.resetOnboarding(authentication.getName());
        return ResponseEntity.ok("Reset complete");
    }
}
