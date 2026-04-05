package back.pickd.auth.oauth;

import back.pickd.auth.jwt.JwtTokenProvider;
import back.pickd.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();

            // 1. Sync User info with DB
            userService.saveOrUpdate(
                    (String) attributes.get("email"),
                    (String) attributes.get("name"),
                    (String) attributes.get("picture")
            );

            // 2. Ensure Google OAuth2 Token is stored in Persistent Storage (JDBC)
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName());
            
            if (client != null) {
                authorizedClientService.saveAuthorizedClient(client, authentication);
            }
        }

        // 3. Issue Service JWT and set in HttpOnly Cookie
        String token = jwtTokenProvider.createToken(authentication);
        setTokenCookie(response, token);

        getRedirectStrategy().sendRedirect(request, response, "/");
    }

    private void setTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 1 day
        response.addCookie(cookie);
    }
}
