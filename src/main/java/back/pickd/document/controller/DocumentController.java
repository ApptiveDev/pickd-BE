package back.pickd.document.controller;

import back.pickd.document.dto.DocumentRequest;
import back.pickd.document.dto.DocumentResponse;
import back.pickd.document.service.DocumentService;
import back.pickd.global.config.OpenApiConfig;
import back.pickd.global.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/document")
@Tag(name = "Document", description = "서류(이력서, 포트폴리오 등) 관리 API")
@SecurityRequirement(name = OpenApiConfig.COOKIE_AUTH)
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    @Operation(summary = "전체 서류 조회", description = "로그인 사용자의 모든 서류를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<DocumentResponse> getAllDocuments(@Parameter(hidden = true) Authentication auth) {
        return documentService.getAllDocuments(auth);
    }

    @GetMapping("/{applicationId}")
    @Operation(summary = "공고별 서류 조회", description = "특정 지원 공고에 속한 서류 목록을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<DocumentResponse> getDocuments(@PathVariable Long applicationId, @Parameter(hidden = true) Authentication auth) {
        return documentService.getDocuments(applicationId, auth);
    }

    @PostMapping("/{applicationId}")
    @Operation(summary = "서류 추가", description = "지원 공고에 새 서류를 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추가 성공",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 유효성 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public DocumentResponse addDocument(@PathVariable Long applicationId,
                                        @RequestBody DocumentRequest request,
                                        @Parameter(hidden = true) Authentication auth) {
        return documentService.addDocument(applicationId, request, auth);
    }

    @PutMapping("/{id}")
    @Operation(summary = "서류 수정", description = "서류 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
            @ApiResponse(responseCode = "403", description = "소유권 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public DocumentResponse updateDocument(@PathVariable Long id,
                                           @RequestBody DocumentRequest request,
                                           @Parameter(hidden = true) Authentication auth) {
        return documentService.updateDocument(id, request, auth);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "서류 삭제", description = "서류를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "소유권 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void deleteDocument(@PathVariable Long id, @Parameter(hidden = true) Authentication auth) {
        documentService.deleteDocument(id, auth);
    }
}
