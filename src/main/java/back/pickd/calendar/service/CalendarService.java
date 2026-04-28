package back.pickd.calendar.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
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

    private Calendar getCalendarClient(Authentication authentication) throws IOException, GeneralSecurityException {
        if (authentication == null) throw new RuntimeException("로그인 필요");

        OAuth2AuthorizedClient client =
                authorizedClientService.loadAuthorizedClient("google", authentication.getName());

        if (client == null) throw new RuntimeException("구글 연동 필요");

        String token = client.getAccessToken().getTokenValue();
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(token, null));

        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        return new Calendar.Builder(httpTransport, GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("Pickd")
                .build();
    }

    private String getOrCreatePickdCalendar(Authentication authentication)
            throws IOException, GeneralSecurityException {

        Calendar service = getCalendarClient(authentication);

        CalendarList calendarList = service.calendarList().list().execute();

        for (CalendarListEntry entry : calendarList.getItems()) {
            if ("Pickd".equals(entry.getSummary())) {
                return entry.getId();
            }
        }

        com.google.api.services.calendar.model.Calendar calendar =
                new com.google.api.services.calendar.model.Calendar();

        calendar.setSummary("Pickd");
        calendar.setTimeZone("Asia/Seoul");

        com.google.api.services.calendar.model.Calendar created =
                service.calendars().insert(calendar).execute();

        return created.getId();
    }

    public List<Event> getEvents(Authentication authentication, DateTime timeMin, DateTime timeMax)
            throws IOException, GeneralSecurityException {

        Calendar service = getCalendarClient(authentication);
        String calendarId = getOrCreatePickdCalendar(authentication);

        Events events = service.events().list(calendarId)
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .setSingleEvents(true)
                .setOrderBy("startTime")
                .execute();

        return events.getItems();
    }
    
    public Event createEvent(Authentication authentication, Event event)
            throws IOException, GeneralSecurityException {

        Calendar service = getCalendarClient(authentication);
        String calendarId = getOrCreatePickdCalendar(authentication);

        return service.events().insert(calendarId, event).execute();
    }

    public Event getEvent(Authentication authentication, String eventId)
            throws IOException, GeneralSecurityException {

        Calendar service = getCalendarClient(authentication);
        String calendarId = getOrCreatePickdCalendar(authentication);

        return service.events().get(calendarId, eventId).execute();
    }

    public Event updateEvent(Authentication authentication, String eventId, Event event)
            throws IOException, GeneralSecurityException {

        Calendar service = getCalendarClient(authentication);
        String calendarId = getOrCreatePickdCalendar(authentication);

        return service.events().update(calendarId, eventId, event).execute();
    }

    public void deleteEvent(Authentication authentication, String eventId)
            throws IOException, GeneralSecurityException {

        Calendar service = getCalendarClient(authentication);
        String calendarId = getOrCreatePickdCalendar(authentication);

        service.events().delete(calendarId, eventId).execute();
    }
}