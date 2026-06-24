package back.pickd.global.infra.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * S3 파일 업로드 API
     * (이력서, 자격증, 수료증 등을 업로드한 후 CloudFront URL을 반환받습니다)
     */
    @PostMapping("/upload")
    public ResponseEntity<UploadedFileResponse> uploadFile(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") FileUploadType type) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(fileService.uploadFile(authentication.getName(), file, type));
    }

    @GetMapping
    public ResponseEntity<List<UploadedFileResponse>> getFiles(
            Authentication authentication,
            @RequestParam(required = false) FileUploadType type) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(fileService.getFiles(authentication.getName(), type));
    }
}
