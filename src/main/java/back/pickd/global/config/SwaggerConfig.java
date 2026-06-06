package back.pickd.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class SwaggerConfig {

    private static final String ERROR_RESPONSE_REF = "#/components/schemas/ErrorResponse";
    private static final String DEFAULT_ERROR_PATH = "/api/experiences";

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "jwtAuth";
        
        // SecurityRequirement 정의 (Bearer 토큰 필요 시 자물쇠 버튼을 활성화하기 위함)
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        
        // SecurityScheme 정의
        SecurityScheme securityScheme = new SecurityScheme()
                .name(jwtSchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT 토큰을 입력해 주세요. (쿠키의 accessToken 값 또는 헤더에 사용할 Bearer 토큰)");

        return new OpenAPI()
                .info(new Info()
                        .title("Pickd API 명세서")
                        .description("AI 기반 개인 맞춤형 채용 관리 플랫폼 Pickd의 API 명세서입니다.<br>" +
                                "로컬 환경에서 구글 OAuth2 로그인 성공 후 발급되는 JWT 토큰을 사용하여 인가된 API들을 테스트할 수 있습니다.")
                        .version("v1.0.0"))
                .addSecurityItem(securityRequirement)
                .components(new Components().addSecuritySchemes(jwtSchemeName, securityScheme));
    }

    @Bean
    public OpenApiCustomizer errorResponseExampleCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> {
                        if (operation.getResponses() != null) {
                            operation.getResponses().forEach(this::applyErrorResponseExample);
                        }
                    })
            );
        };
    }

    private void applyErrorResponseExample(String responseCode, ApiResponse response) {
        Content content = response.getContent();
        if (content == null || content.isEmpty()) {
            return;
        }

        MediaType errorMediaType = content.values().stream()
                .filter(this::usesErrorResponseSchema)
                .findFirst()
                .orElse(null);
        if (errorMediaType == null) {
            return;
        }

        errorMediaType.setExample(errorExample(responseCode));
        content.clear();
        content.addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, errorMediaType);
    }

    private boolean usesErrorResponseSchema(MediaType mediaType) {
        Schema<?> schema = mediaType.getSchema();
        return schema != null && ERROR_RESPONSE_REF.equals(schema.get$ref());
    }

    private Map<String, Object> errorExample(String responseCode) {
        HttpStatus status = parseStatus(responseCode);
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("timestamp", "2026-06-03T15:43:33.594");
        example.put("status", status.value());
        example.put("error", status.getReasonPhrase());
        example.put("message", exampleMessage(status));
        example.put("path", DEFAULT_ERROR_PATH);
        return example;
    }

    private HttpStatus parseStatus(String responseCode) {
        try {
            HttpStatus status = HttpStatus.resolve(Integer.parseInt(responseCode));
            return status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (NumberFormatException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private String exampleMessage(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "요청값이 올바르지 않습니다.";
            case UNAUTHORIZED -> "인증이 필요합니다.";
            case FORBIDDEN -> "접근 권한이 없습니다.";
            case NOT_FOUND -> "요청한 리소스를 찾을 수 없습니다.";
            case PAYLOAD_TOO_LARGE -> "업로드 가능한 파일 크기를 초과했습니다.";
            case BAD_GATEWAY -> "외부 API 연동 중 오류가 발생했습니다.";
            case INTERNAL_SERVER_ERROR -> "서버 내부 오류가 발생했습니다.";
            default -> status.is4xxClientError()
                    ? "요청 처리 중 클라이언트 오류가 발생했습니다."
                    : "요청 처리 중 서버 오류가 발생했습니다.";
        };
    }
}
