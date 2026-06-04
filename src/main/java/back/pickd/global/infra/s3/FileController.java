package back.pickd.global.infra.s3;

import back.pickd.global.error.ApiException;
import back.pickd.global.error.ErrorResponse;
import back.pickd.user.entity.User;
import back.pickd.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "파일 (Files)", description = "S3/CloudFront 기반 사용자 파일 업로드 API")
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
    @Operation(
            summary = "파일 업로드",
            description = "이력서, 자격증, 수료증 등 사용자 파일을 S3에 업로드하고 CloudFront 접근 URL과 업로드 메타데이터를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "파일 업로드 성공",
                    content = @Content(schema = @Schema(type = "object", example = "{\"fileUrl\":\"https://cdn.pickd.co.kr/experience/general/1/example.pdf\",\"fileName\":\"resume.pdf\",\"uploadType\":\"GENERAL\"}"),
                            examples = @ExampleObject(value = "{\"fileUrl\":\"https://cdn.pickd.co.kr/experience/general/1/example.pdf\",\"fileName\":\"resume.pdf\",\"uploadType\":\"GENERAL\"}"))),
            @ApiResponse(responseCode = "400", description = "파일이 비어 있거나 업로드 타입이 올바르지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "S3 업로드 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(
            Authentication authentication,
            @Parameter(description = "업로드할 파일", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "파일 업로드 분류", required = true, example = "GENERAL")
            @RequestParam("type") FileUploadType type) {
        
        if (authentication == null) {
            throw ApiException.unauthorized("로그인이 필요합니다.");
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
