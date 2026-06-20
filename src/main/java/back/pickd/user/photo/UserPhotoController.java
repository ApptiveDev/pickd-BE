package back.pickd.user.photo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/user/photos")
@RequiredArgsConstructor
public class UserPhotoController {

    private final UserPhotoService userPhotoService;

    @PostMapping
    public ResponseEntity<UserPhotoResponse> uploadPhoto(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean isPublic,
            @RequestParam(defaultValue = "false") boolean isRepresentative) {
        return ResponseEntity.ok(
                userPhotoService.uploadPhoto(authentication.getName(), file, isPublic, isRepresentative)
        );
    }

    @GetMapping
    public ResponseEntity<List<UserPhotoResponse>> getPhotos(Authentication authentication) {
        return ResponseEntity.ok(userPhotoService.getPhotos(authentication.getName()));
    }

    @PutMapping("/{photoId}/representative")
    public ResponseEntity<UserPhotoResponse> updateRepresentative(
            Authentication authentication,
            @PathVariable Long photoId) {
        return ResponseEntity.ok(
                userPhotoService.updateRepresentative(authentication.getName(), photoId)
        );
    }
}
