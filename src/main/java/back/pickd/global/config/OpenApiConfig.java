package back.pickd.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String COOKIE_AUTH = "cookieAuth";

    @Bean
    public OpenAPI pickdOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("PICKD API")
                        .version("v2")
                        .description("PICKD Spring 서버 REST API 명세"))
                .components(new Components()
                        .addSecuritySchemes(
                                COOKIE_AUTH,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("accessToken")
                                        .description("로그인 후 발급되는 JWT accessToken 쿠키")
                        ));
    }
}
