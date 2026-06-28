package back.pickd.notice.controller;

import back.pickd.global.config.OpenApiConfig;
import back.pickd.global.error.ErrorResponse;
import back.pickd.notice.dto.UrlAnalysisRequestDto;
import back.pickd.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@Tag(name = "Notice", description = "채용공고 AI 분석 API")
@SecurityRequirement(name = OpenApiConfig.COOKIE_AUTH)
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping("/analyze/url")
    @Operation(
            summary = "URL 기반 채용공고 분석",
            description = "채용공고 URL을 AI로 분석하여 저장하고 noticeId를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "분석 및 저장 성공",
                    content = @Content(schema = @Schema(example = "{\"noticeId\": 1}"))),
            @ApiResponse(responseCode = "400", description = "URL 유효성 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, Long>> analyzeNoticeUrl(
            @Parameter(hidden = true) Authentication authentication,
            @RequestBody @Valid UrlAnalysisRequestDto request) {
        Long noticeId = noticeService.analyzeAndSaveNoticeUrl(authentication.getName(), request.getUrl());
        return ResponseEntity.ok(Map.of("noticeId", noticeId));
    }

    @PostMapping(value = "/analyze/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "PDF 기반 채용공고 분석",
            description = "채용공고 PDF 파일을 AI로 분석하여 저장하고 noticeId를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "분석 및 저장 성공",
                    content = @Content(schema = @Schema(example = "{\"noticeId\": 1}"))),
            @ApiResponse(responseCode = "400", description = "파일 누락 또는 AI 분석 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, Long>> analyzeNoticePdf(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "채용공고 PDF 파일", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file) {
        Long noticeId = noticeService.analyzeAndSaveNoticePdf(authentication.getName(), file);
        return ResponseEntity.ok(Map.of("noticeId", noticeId));
    }
}
