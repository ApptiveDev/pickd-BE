package back.pickd.user.controller;

import back.pickd.user.dto.UserResponseDto;
import back.pickd.user.dto.onboarding.OnboardingRequest;
import back.pickd.user.entity.User;
import back.pickd.global.error.ErrorResponse;
import back.pickd.user.service.OnboardingService;
import back.pickd.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "온보딩 (Onboarding)", description = "약관, 기본 정보, 학력, 관심 직무, 취업 준비 상태를 저장하고 조회하는 API")
@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final UserService userService;

    @Operation(
            summary = "온보딩 정보 저장",
            description = "현재 로그인한 사용자의 온보딩 입력값을 저장합니다. 요청에 포함된 필드만 단계별로 반영됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "온보딩 정보 저장 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "요청값이 올바르지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "온보딩 단계별 입력 데이터",
            required = true,
            content = @Content(schema = @Schema(implementation = OnboardingRequest.class))
    )
    @PostMapping
    public ResponseEntity<UserResponseDto> updateOnboarding(Authentication authentication, @RequestBody @Valid OnboardingRequest request) {
        User user = onboardingService.updateOnboarding(authentication.getName(), request);
        return ResponseEntity.ok(UserResponseDto.from(user));
    }

    @Operation(
            summary = "온보딩 상태 조회",
            description = "현재 로그인한 사용자의 온보딩 진행 단계와 주요 프로필 요약 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "온보딩 상태 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/status")
    public ResponseEntity<UserResponseDto> getStatus(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(UserResponseDto.from(user));
    }

    @Operation(
            summary = "온보딩 정보 초기화",
            description = "현재 로그인한 사용자의 온보딩 관련 정보를 초기화하고 진행 단계를 처음 상태로 되돌립니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "온보딩 초기화 성공",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/reset")
    public ResponseEntity<String> reset(Authentication authentication) {
        onboardingService.resetOnboarding(authentication.getName());
        return ResponseEntity.ok("Reset complete");
    }
}
