package back.pickd.global.infra.ai;

import back.pickd.global.infra.ai.dto.AiStep1Response;
import back.pickd.global.infra.ai.dto.AiStep2Response;
import back.pickd.global.infra.ai.dto.AiJobPostingResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Component
public class AiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AiClient(@Value("${ai-server.url:http://localhost:8000}") String aiServerUrl, ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(180000); // 180초 타임아웃 설정

        this.restClient = RestClient.builder()
                .baseUrl(aiServerUrl)
                .requestFactory(requestFactory)
                .build();
        this.objectMapper = objectMapper;
    }

    /**
     * AI 1차 경험 추출 (step1)
     */
    public AiStep1Response extractStep1(MultipartFile file) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", file.getResource());

        return restClient.post()
                .uri("/api/v1/extract-experiences/step1")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(bodyBuilder.build())
                .retrieve()
                .body(AiStep1Response.class);
    }

    /**
     * AI 2차 정밀 경험 추출 (step2)
     */
    public AiStep2Response extractStep2(MultipartFile file, List<AiStep1Response.ExperienceSummaryDto> selectedExperiences) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", file.getResource());

        try {
            // 선택한 1차 경험 리스트를 JSON String으로 변환하여 폼 전송
            String jsonList = objectMapper.writeValueAsString(selectedExperiences);
            bodyBuilder.part("selected_experiences", jsonList);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize selected experiences for AI Step2", e);
            throw new RuntimeException("AI 분석 요청 중 직렬화 오류가 발생했습니다.", e);
        }

        return restClient.post()
                .uri("/api/v1/extract-experiences/step2")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(bodyBuilder.build())
                .retrieve()
                .body(AiStep2Response.class);
    }

    /**
     * AI 2차 정밀 경험 추출 (step2) - S3 CloudFront URL 기반 호출
     */
    public AiStep2Response extractStep2ByUrl(String resumeUrl, List<AiStep1Response.ExperienceSummaryDto> selectedExperiences) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("url", resumeUrl);

        try {
            String jsonList = objectMapper.writeValueAsString(selectedExperiences);
            bodyBuilder.part("selected_experiences", jsonList);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize selected experiences for AI Step2", e);
            throw new RuntimeException("AI 분석 요청 중 직렬화 오류가 발생했습니다.", e);
        }

        return restClient.post()
                .uri("/api/v1/extract-experiences/step2")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(bodyBuilder.build())
                .retrieve()
                .body(AiStep2Response.class);
    }

    public AiJobPostingResponse analyzeNoticeUrl(String url) {
        return restClient.post()
                .uri("/api/v1/analyze/url")
                .contentType(MediaType.APPLICATION_JSON)
                .body(java.util.Map.of("url", url))
                .retrieve()
                .body(AiJobPostingResponse.class);
    }

    public AiJobPostingResponse analyzeNoticePdf(MultipartFile file) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", file.getResource());

        return restClient.post()
                .uri("/api/v1/analyze/pdf")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(bodyBuilder.build())
                .retrieve()
                .body(AiJobPostingResponse.class);
    }
}
