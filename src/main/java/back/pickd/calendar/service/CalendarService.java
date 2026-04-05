package back.pickd.calendar.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final OAuth2AuthorizedClientService authorizedClientService;

    /**
     * Google Calendar API 클라이언트를 생성하여 반환합니다.
     */
    private Calendar getCalendarClient(Authentication authentication) throws IOException, GeneralSecurityException {
        if (authentication == null) {
            throw new RuntimeException("인증 정보가 유효하지 않습니다. 다시 로그인해 주세요.");
        }

        // DB/메모리 저장소에서 해당 유저의 구글 인증 토큰을 조회
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("google", authentication.getName());

        if (client == null) {
            log.error("User [{}] has no authorized Google client found in storage.", authentication.getName());
            throw new RuntimeException("구글 인증 정보를 찾을 수 없습니다. 구글 계정 연동이 필요합니다.");
        }

        String tokenValue = client.getAccessToken().getTokenValue();
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(tokenValue, null));

        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Calendar.Builder(httpTransport, GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                .setApplicationName("Pickd")
                .build();
    }

    public List<Event> getEvents(Authentication authentication, DateTime timeMin, DateTime timeMax) throws IOException, GeneralSecurityException {
        Calendar calendar = getCalendarClient(authentication);
        Events events = calendar.events().list("primary")
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .setSingleEvents(true)
                .setOrderBy("startTime")
                .execute();
        return events.getItems();
    }

    public Event createEvent(Authentication authentication, Event event) throws IOException, GeneralSecurityException {
        return getCalendarClient(authentication).events().insert("primary", event).execute();
    }

    public Event getEvent(Authentication authentication, String eventId) throws IOException, GeneralSecurityException {
        return getCalendarClient(authentication).events().get("primary", eventId).execute();
    }

    public Event updateEvent(Authentication authentication, String eventId, Event event) throws IOException, GeneralSecurityException {
        return getCalendarClient(authentication).events().update("primary", eventId, event).execute();
    }

    public void deleteEvent(Authentication authentication, String eventId) throws IOException, GeneralSecurityException {
        getCalendarClient(authentication).events().delete("primary", eventId).execute();
    }
}
