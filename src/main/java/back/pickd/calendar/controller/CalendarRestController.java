package back.pickd.calendar.controller;

import back.pickd.calendar.dto.CalendarEventRequest;
import back.pickd.calendar.service.CalendarService;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarRestController {

    private final CalendarService calendarService;

    /**
     * 일정 목록 조회
     */
    @GetMapping("/events")
    public List<Event> getEvents(Authentication authentication,
                                @RequestParam(required = false) String timeMin,
                                @RequestParam(required = false) String timeMax) throws IOException, GeneralSecurityException {
        
        DateTime min = (timeMin != null) ? new DateTime(timeMin) : new DateTime(System.currentTimeMillis());
        DateTime max = (timeMax != null) ? new DateTime(timeMax) : new DateTime(System.currentTimeMillis() + 31536000000L);
        
        return calendarService.getEvents(authentication, min, max);
    }

    /**
     * 일정 등록
     */
    @PostMapping("/events")
    public Event createEvent(Authentication authentication, @RequestBody CalendarEventRequest requestDto) throws IOException, GeneralSecurityException {
        Event event = new Event()
                .setSummary(requestDto.getSummary())
                .setLocation(requestDto.getLocation())
                .setDescription(requestDto.getDescription());

        if (requestDto.getStart() != null) {
            event.setStart(new EventDateTime()
                    .setDateTime(new DateTime(requestDto.getStart().getDateTime()))
                    .setTimeZone(requestDto.getStart().getTimeZone()));
        }

        if (requestDto.getEnd() != null) {
            event.setEnd(new EventDateTime()
                    .setDateTime(new DateTime(requestDto.getEnd().getDateTime()))
                    .setTimeZone(requestDto.getEnd().getTimeZone()));
        }

        return calendarService.createEvent(authentication, event);
    }

    /**
     * 일정 수정 (부분 업데이트 지원)
     */
    @PutMapping("/events/{eventId}")
    public Event updateEvent(Authentication authentication, 
                            @PathVariable String eventId, 
                            @RequestBody CalendarEventRequest requestDto) throws IOException, GeneralSecurityException {
        
        Event existingEvent = calendarService.getEvent(authentication, eventId);
        
        if (requestDto.getSummary() != null) existingEvent.setSummary(requestDto.getSummary());
        if (requestDto.getLocation() != null) existingEvent.setLocation(requestDto.getLocation());
        if (requestDto.getDescription() != null) existingEvent.setDescription(requestDto.getDescription());
        
        return calendarService.updateEvent(authentication, eventId, existingEvent);
    }

    /**
     * 일정 삭제
     */
    @DeleteMapping("/events/{eventId}")
    public void deleteEvent(Authentication authentication, @PathVariable String eventId) throws IOException, GeneralSecurityException {
        calendarService.deleteEvent(authentication, eventId);
    }
}