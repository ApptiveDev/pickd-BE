package back.pickd.user.controller;

import back.pickd.global.config.OpenApiConfig;
import back.pickd.global.error.ErrorResponse;
import back.pickd.user.dto.UserProfileDto;
import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 프로필 조회 및 프로필 이미지 관리 API")
@SecurityRequirement(name = OpenApiConfig.COOKIE_AUTH)
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "내 프로필 조회", description = "로그인 사용자의 전체 프로필 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserProfileDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserProfileDto> getUser(@Parameter(hidden = true) Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(UserProfileDto.from(user));
    }

    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 이미지 업로드", description = "프로필 이미지를 S3에 업로드하고 URL을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 성공",
                    content = @Content(schema = @Schema(example = "{\"profileImageUrl\": \"https://cdn.example.com/image.jpg\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "업로드할 이미지 파일", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file) {
        String profileImageUrl = userService.updateProfileImage(authentication.getName(), file);
        return ResponseEntity.ok(Map.of("profileImageUrl", profileImageUrl));
    }

    @GetMapping("/profile-image")
    @Operation(summary = "프로필 이미지 URL 조회", description = "현재 등록된 프로필 이미지 URL을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(example = "{\"profileImageUrl\": \"https://cdn.example.com/image.jpg\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, String>> getProfileImage(@Parameter(hidden = true) Authentication authentication) {
        String profileImageUrl = userService.getProfileImage(authentication.getName());
        return ResponseEntity.ok(Map.of("profileImageUrl", profileImageUrl));
    }
}
