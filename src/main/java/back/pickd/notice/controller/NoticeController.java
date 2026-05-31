package back.pickd.notice.controller;

import back.pickd.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

// 채용공고 AI 분석 및 저장을 위한 REST 컨트롤러
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // URL 기반 채용공고 분석 API
    @PostMapping("/analyze/url")
    public ResponseEntity<Map<String, Long>> analyzeNoticeUrl(
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL is required");
        }
        Long noticeId = noticeService.analyzeAndSaveNoticeUrl(authentication.getName(), url);
        return ResponseEntity.ok(Map.of("noticeId", noticeId));
    }

    // PDF 파일 기반 채용공고 분석 API
    @PostMapping("/analyze/pdf")
    public ResponseEntity<Map<String, Long>> analyzeNoticePdf(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        Long noticeId = noticeService.analyzeAndSaveNoticePdf(authentication.getName(), file);
        return ResponseEntity.ok(Map.of("noticeId", noticeId));
    }
}
