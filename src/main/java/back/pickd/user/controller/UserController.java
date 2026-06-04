package back.pickd.user.controller;

import back.pickd.user.dto.UserResponseDto;
import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
import back.pickd.global.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자 (User)", description = "현재 로그인한 사용자 프로필 조회 API")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "현재 사용자 조회",
            description = "JWT 인증 정보의 이메일을 기준으로 현재 로그인한 사용자의 기본 프로필을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<UserResponseDto> getUser(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());

        return ResponseEntity.ok(
                UserResponseDto.builder()
                        .nickname(user.getNickname())
                        .build()
        );
    }
}
