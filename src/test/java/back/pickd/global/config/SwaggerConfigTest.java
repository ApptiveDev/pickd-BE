package back.pickd.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SwaggerConfigTest {

    private final SwaggerConfig swaggerConfig = new SwaggerConfig();

    @Test
    void errorResponseExampleUsesResponseCodeStatus() {
        ApiResponse unauthorizedResponse = new ApiResponse()
                .content(new Content().addMediaType("*/*", errorResponseMediaType()));
        OpenAPI openApi = new OpenAPI()
                .paths(new Paths().addPathItem("/api/user", new PathItem().get(new Operation()
                        .responses(new ApiResponses().addApiResponse("401", unauthorizedResponse)))));

        swaggerConfig.errorResponseExampleCustomizer().customise(openApi);

        MediaType jsonMediaType = unauthorizedResponse.getContent().get(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
        assertNotNull(jsonMediaType);

        @SuppressWarnings("unchecked")
        Map<String, Object> example = (Map<String, Object>) jsonMediaType.getExample();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), example.get("status"));
        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), example.get("error"));
        assertEquals("인증이 필요합니다.", example.get("message"));
    }

    @Test
    void nonErrorResponseSchemaIsNotChanged() {
        MediaType successMediaType = new MediaType().schema(new Schema<>().type("string"));
        ApiResponse successResponse = new ApiResponse()
                .content(new Content().addMediaType("*/*", successMediaType));
        OpenAPI openApi = new OpenAPI()
                .paths(new Paths().addPathItem("/api/calendar/me", new PathItem().get(new Operation()
                        .responses(new ApiResponses().addApiResponse("200", successResponse)))));

        swaggerConfig.errorResponseExampleCustomizer().customise(openApi);

        assertEquals(successMediaType, successResponse.getContent().get("*/*"));
    }

    private MediaType errorResponseMediaType() {
        return new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"));
    }
}
