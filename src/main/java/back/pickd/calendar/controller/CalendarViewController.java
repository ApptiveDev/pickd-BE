package back.pickd.calendar.controller;

import back.pickd.calendar.service.CalendarService;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.TimeZone;
import java.util.Calendar;

@RestController
@RequiredArgsConstructor
public class CalendarViewController {

    private final CalendarService calendarService;

    @GetMapping("/")
    public String index(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/my-calendar";
        }
        return "index";
    }

    @GetMapping("/api/calendar")
    public List<Event> getCalendar(Authentication authentication) throws Exception {
        TimeZone tz = TimeZone.getTimeZone("Asia/Seoul");
        Calendar cal = Calendar.getInstance(tz);

        cal.add(Calendar.DAY_OF_YEAR, -14);
        DateTime timeMin = new DateTime(cal.getTime());

        cal.add(Calendar.DAY_OF_YEAR, 28);
        DateTime timeMax = new DateTime(cal.getTime());

        return calendarService.getEvents(authentication, timeMin, timeMax);
    }

    @PostMapping("/api/calendar")
    public void createEvent(Authentication auth, @RequestBody Event event) {
        try {
            calendarService.createEvent(auth, event);
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("캘린더 생성 실패", e);
        }
    }
}
