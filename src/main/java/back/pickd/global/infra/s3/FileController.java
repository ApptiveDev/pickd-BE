package back.pickd.global.infra.s3;

import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final S3Service s3Service;
    private final UserService userService;

    /**
     * S3 파일 업로드 API
     * (이력서, 자격증, 수료증 등을 업로드한 후 CloudFront URL을 반환받습니다)
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") FileUploadType type) {
        
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        // 1. 현재 로그인된 사용자 정보 조회
        User user = userService.findByEmail(authentication.getName());
        
        // 2. S3Service를 호출하여 S3에 저장하고 CloudFront CDN URL 획득
        String fileUrl = s3Service.uploadFile(file, type, user.getId());

        // 3. 응답 맵 구성
        Map<String, String> response = new HashMap<>();
        response.put("fileUrl", fileUrl);
        response.put("fileName", file.getOriginalFilename());
        response.put("uploadType", type.name());

        return ResponseEntity.ok(response);
    }
}
