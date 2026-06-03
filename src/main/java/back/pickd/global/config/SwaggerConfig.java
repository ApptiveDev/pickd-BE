package back.pickd.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

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
}
