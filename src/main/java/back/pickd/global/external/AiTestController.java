package back.pickd.global.external;

import back.pickd.notice.dto.ai.AiJobPostingResponse;
import back.pickd.user.dto.ai.AiExperienceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/test/ai")
@RequiredArgsConstructor
public class AiTestController {

    private final AiClient aiClient;

    /**
     * 1. 공고 URL 분석 테스트
     * GET http://localhost:8080/api/test/ai/jd?url=공고주소
     */
    @GetMapping("/jd")
    public AiJobPostingResponse testJd(@RequestParam String url) {
        return aiClient.analyzeJobPostingUrl(url);
    }

    /**
     * 2. 공고 PDF 파일 분석 테스트
     * POST http://localhost:8080/api/test/ai/jd/file
     * Body: form-data (key: file, value: [PDF 파일])
     */
    @PostMapping("/jd/file")
    public AiJobPostingResponse testJdFile(@RequestParam("file") MultipartFile file) {
        return aiClient.analyzeJobPostingPdf(file.getResource());
    }

    /**
     * 3. 자소서 경험 추출 테스트 (텍스트 입력 방식)
     * POST http://localhost:8080/api/test/ai/experience
     * Body: { "text": "자소서 내용..." }
     */
    @PostMapping("/experience")
    public AiExperienceResponseDto testExperience(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return aiClient.extractExperiences(null, null, text);
    }

    /**
     * 4. 자소서 PDF 파일 경험 추출 테스트
     * POST http://localhost:8080/api/test/ai/experience/file
     * Body: form-data (key: file, value: [PDF 파일])
     */
    @PostMapping("/experience/file")
    public AiExperienceResponseDto testExperienceFile(@RequestParam("file") MultipartFile file) {
        return aiClient.extractExperiences(file.getResource(), null, null);
    }
}
