package back.pickd.global.external;

import back.pickd.notice.dto.ai.AiJobPostingResponse;
import back.pickd.user.dto.ai.AiExperienceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AiClient {

    private final RestClient aiRestClient;

    /**
     * 채용 공고 PDF 분석 요청
     */
    public AiJobPostingResponse analyzeJobPostingPdf(Resource pdfResource) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", pdfResource);

        return aiRestClient.post()
                .uri("/api/v1/analyze/pdf")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(AiJobPostingResponse.class);
    }

    /**
     * 채용 공고 URL 분석 요청
     */
    public AiJobPostingResponse analyzeJobPostingUrl(String url) {
        Map<String, String> body = Map.of("url", url);

        return aiRestClient.post()
                .uri("/api/v1/analyze/url")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(AiJobPostingResponse.class);
    }

    /**
     * 자소서 경험 추출 요청
     */
    public AiExperienceResponseDto extractExperiences(Resource file, String url, String text) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        if (file != null) body.add("file", file);
        if (url != null) body.add("url", url);
        if (text != null) body.add("text", text);

        return aiRestClient.post()
                .uri("/api/v1/extract-experiences")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(AiExperienceResponseDto.class);
    }
}
