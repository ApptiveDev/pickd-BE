package back.pickd.auth.oauth;

import back.pickd.auth.jwt.JwtTokenProvider;
import back.pickd.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String email = (String) attributes.get("email");

            userService.saveOrUpdate(
                    email,
                    (String) attributes.get("name"),
                    (String) attributes.get("picture")
            );

            // JWT 발급 및 쿠키 설정 (핵심 로그인 흐름 — 먼저 처리)
            String token = jwtTokenProvider.createToken(email, authentication.getAuthorities());
            setTokenCookie(response, token);

            // Google Calendar/Drive 클라이언트 저장 (실패해도 로그인은 정상 처리)
            try {
                OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                        oauthToken.getAuthorizedClientRegistrationId(),
                        oauthToken.getName()
                );
                if (client != null) {
                    UsernamePasswordAuthenticationToken newAuth =
                            new UsernamePasswordAuthenticationToken(email, null, authentication.getAuthorities());
                    authorizedClientService.saveAuthorizedClient(client, newAuth);
                }
            } catch (Exception e) {
                log.warn("OAuth2 authorized client 저장 실패 (캘린더/드라이브 연동 불가): {}", e.getMessage());
            }

            response.sendRedirect("http://localhost:5173/onboarding");
        }
    }

    private void setTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);
    }
}
