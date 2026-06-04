package back.pickd.notice.controller;

import back.pickd.notice.dto.UrlAnalysisRequestDto;
import back.pickd.notice.service.NoticeService;
import back.pickd.global.error.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

// 채용공고 AI 분석 및 저장을 위한 REST 컨트롤러
@Tag(name = "채용공고 분석 (Notices)", description = "채용공고 URL/PDF를 AI로 분석하고 공고 데이터를 저장하는 API")
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // URL 기반 채용공고 분석 API
    @Operation(
            summary = "URL 기반 채용공고 분석",
            description = "채용공고 URL을 AI 서버로 분석하고 공고, 모집부문, 전형 단계, 제출 서류 정보를 저장한 뒤 noticeId를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채용공고 URL 분석 및 저장 성공",
                    content = @Content(schema = @Schema(type = "object", example = "{\"noticeId\":1}"),
                            examples = @ExampleObject(value = "{\"noticeId\":1}"))),
            @ApiResponse(responseCode = "400", description = "URL이 비어 있거나 요청값이 올바르지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "AI 서버 연동 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "분석할 채용공고 URL",
            required = true,
            content = @Content(schema = @Schema(implementation = UrlAnalysisRequestDto.class))
    )
    @PostMapping("/analyze/url")
    public ResponseEntity<Map<String, Long>> analyzeNoticeUrl(
            Authentication authentication,
            @RequestBody @Valid UrlAnalysisRequestDto request) {
        Long noticeId = noticeService.analyzeAndSaveNoticeUrl(authentication.getName(), request.getUrl());
        return ResponseEntity.ok(Map.of("noticeId", noticeId));
    }

    // PDF 파일 기반 채용공고 분석 API
    @Operation(
            summary = "PDF 기반 채용공고 분석",
            description = "채용공고 PDF 파일을 AI 서버로 분석하고 공고, 모집부문, 전형 단계, 제출 서류 정보를 저장한 뒤 noticeId를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채용공고 PDF 분석 및 저장 성공",
                    content = @Content(schema = @Schema(type = "object", example = "{\"noticeId\":1}"),
                            examples = @ExampleObject(value = "{\"noticeId\":1}"))),
            @ApiResponse(responseCode = "400", description = "PDF 파일이 없거나 비어 있음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "AI 서버 연동 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/analyze/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Long>> analyzeNoticePdf(
            Authentication authentication,
            @Parameter(description = "분석할 채용공고 PDF 파일", required = true)
            @RequestParam("file") MultipartFile file) {
        Long noticeId = noticeService.analyzeAndSaveNoticePdf(authentication.getName(), file);
        return ResponseEntity.ok(Map.of("noticeId", noticeId));
    }
}
