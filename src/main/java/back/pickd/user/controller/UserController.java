package back.pickd.user.controller;

import back.pickd.user.dto.UserResponseDto;
import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponseDto> getUser(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());

        return ResponseEntity.ok(
                UserResponseDto.builder()
                        .nickname(user.getNickname())
                        .build()
        );
    }

    @PostMapping("/profile-image")
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        String profileImageUrl = userService.updateProfileImage(authentication.getName(), file);
        return ResponseEntity.ok(Map.of("profileImageUrl", profileImageUrl));
    }

    @GetMapping("/profile-image")
    public ResponseEntity<Map<String, String>> getProfileImage(Authentication authentication) {
        String profileImageUrl = userService.getProfileImage(authentication.getName());
        return ResponseEntity.ok(Map.of("profileImageUrl", profileImageUrl));
    }
}
