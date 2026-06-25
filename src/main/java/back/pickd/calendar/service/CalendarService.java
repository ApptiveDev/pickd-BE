package back.pickd.calendar.service;

import back.pickd.application.entity.Todo;
import back.pickd.global.error.ApiException;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {
    private final OAuth2AuthorizedClientService authorizedClientService;
    private static final String TIME_ZONE = "Asia/Seoul";
    private static final ZoneId SEOUL_ZONE = ZoneId.of(TIME_ZONE);

    private Calendar getCalendarClient(Authentication authentication) throws IOException, GeneralSecurityException {
        if (authentication == null) throw ApiException.unauthorized("로그인이 필요합니다.");
        OAuth2AuthorizedClient client =
                authorizedClientService.loadAuthorizedClient("google", authentication.getName());

        if (client == null) throw ApiException.unauthorized("Google 계정 연동이 필요합니다.");
        String token = client.getAccessToken().getTokenValue();
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(token, null));
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        return new Calendar.Builder(
                httpTransport,
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        )
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
        calendar.setTimeZone(TIME_ZONE);

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

        if (eventId == null || eventId.isBlank()) {
            return;
        }

        Calendar service = getCalendarClient(authentication);
        String calendarId = getOrCreatePickdCalendar(authentication);

        service.events().delete(calendarId, eventId).execute();
    }

    public Event createTodoEvent(Authentication authentication, Todo todo)
            throws IOException, GeneralSecurityException {

        if (todo == null) {
            throw new IllegalArgumentException("Todo가 비어 있습니다.");
        }

        if (todo.getDueDateTime() == null) {
            throw new IllegalArgumentException("할 일 마감 시간이 없습니다.");
        }

        ZonedDateTime startDateTime = todo.getDueDateTime().atZone(SEOUL_ZONE);
        ZonedDateTime endDateTime = startDateTime.plusMinutes(30);

        Event event = new Event()
                .setSummary("[할일] " + todo.getTitle())
                .setDescription(makeTodoDescription(todo))
                .setStart(new EventDateTime()
                        .setDateTime(new DateTime(startDateTime.toInstant().toEpochMilli()))
                        .setTimeZone(TIME_ZONE))
                .setEnd(new EventDateTime()
                        .setDateTime(new DateTime(endDateTime.toInstant().toEpochMilli()))
                        .setTimeZone(TIME_ZONE));

        return createEvent(authentication, event);
    }

    private String makeTodoDescription(Todo todo) {
        StringBuilder sb = new StringBuilder();

        sb.append("category:todo");

        if (todo.getMemo() != null && !todo.getMemo().isBlank()) {
            sb.append("\n\n").append(todo.getMemo());
        }

        if (todo.getApplication() != null) {
            sb.append("\n\n공고: ");
            if (todo.getApplication().getCompany() != null) {
                sb.append(todo.getApplication().getCompany());
            }
            if (todo.getApplication().getJobTitle() != null) {
                sb.append(" - ").append(todo.getApplication().getJobTitle());
            }
        }
        return sb.toString();
    }
}