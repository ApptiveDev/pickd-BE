package back.pickd.notice.controller;

import back.pickd.global.config.OpenApiConfig;
import back.pickd.global.error.ErrorResponse;
import back.pickd.notice.dto.UrlAnalysisRequestDto;
import back.pickd.notice.dto.response.NoticeDetailResponse;
import back.pickd.notice.dto.response.NoticeListResponse;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@Tag(name = "Notice", description = "채용공고 AI 분석 API")
@SecurityRequirement(name = OpenApiConfig.COOKIE_AUTH)
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    @Operation(summary = "공고 목록 조회", description = "로그인 사용자의 전체 공고 목록을 최신순으로 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<NoticeListResponse>> getNotices(
            @Parameter(hidden = true) Authentication authentication) {
        return ResponseEntity.ok(noticeService.getNotices(authentication.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "공고 상세 조회", description = "noticeId로 공고 및 연결된 모집부문, 전형단계, 제출서류를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "공고를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<NoticeDetailResponse> getNoticeDetail(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        return ResponseEntity.ok(noticeService.getNoticeDetail(authentication.getName(), id));
    }

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

    @PostMapping(value = "/analyze/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "이미지 기반 채용공고 분석",
            description = "채용공고 이미지(PNG, JPG 등) 1장 이상을 업로드하면 Gemini Flash로 분석하여 저장하고 noticeId를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "분석 및 저장 성공",
                    content = @Content(schema = @Schema(example = "{\"noticeId\": 1}"))),
            @ApiResponse(responseCode = "400", description = "파일 누락 또는 AI 분석 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, Long>> analyzeNoticeImages(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "채용공고 이미지 파일 (여러 장 가능)", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("files") List<MultipartFile> files) {
        Long noticeId = noticeService.analyzeAndSaveNoticeImages(authentication.getName(), files);
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
