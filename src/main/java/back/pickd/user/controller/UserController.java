package back.pickd.user.controller;

import back.pickd.user.dto.UserResponseDto;
import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}