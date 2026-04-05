package back.pickd.calendar.controller;

import back.pickd.calendar.service.CalendarService;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Controller
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

    @GetMapping("/my-calendar")
    public String viewCalendar(Authentication authentication, Model model) throws IOException, GeneralSecurityException {
        java.util.TimeZone tz = java.util.TimeZone.getTimeZone("Asia/Seoul");
        java.util.Calendar cal = java.util.Calendar.getInstance(tz);

        cal.add(java.util.Calendar.DAY_OF_YEAR, -14);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        DateTime timeMin = new DateTime(cal.getTime());

        cal.add(java.util.Calendar.DAY_OF_YEAR, 28);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        DateTime timeMax = new DateTime(cal.getTime());

        List<Event> items = calendarService.getEvents(authentication, timeMin, timeMax);

        model.addAttribute("events", items);
        return "calendar";
    }
}