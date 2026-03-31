package back.pickd.googleCalender;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class CalendarViewController {

    private final Calendar calendar;

    public CalendarViewController(Calendar calendar) {
        this.calendar = calendar;
    }

    @GetMapping("/my-calendar")
    public String viewCalendar(Model model) throws IOException {
        // 1. 한국 시간대 및 기준 시간 설정
        java.util.TimeZone tz = java.util.TimeZone.getTimeZone("Asia/Seoul");
        java.util.Calendar cal = java.util.Calendar.getInstance(tz);

        // 현재 시간을 저장해둡니다.
        long nowMillis = cal.getTimeInMillis();

        // 2. 시작 시간 설정 (오늘로부터 14일 전, 00:00:00)
        cal.add(java.util.Calendar.DAY_OF_YEAR, -14);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        DateTime timeMin = new DateTime(cal.getTime());

        // 3. 종료 시간 설정 (시작 시간으로부터 28일 뒤 = 오늘로부터 14일 뒤, 23:59:59)
        cal.add(java.util.Calendar.DAY_OF_YEAR, 28);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        DateTime timeMax = new DateTime(cal.getTime());

        // 디버깅 로그
        System.out.println("조회 범위: " + timeMin + " ~ " + timeMax);

        // 4. API 호출
        Events events = calendar.events().list("primary")
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .setSingleEvents(true)
                .setOrderBy("startTime")
                .setMaxResults(250) // 일정이 많을 경우를 대비해 넉넉히 설정
                .execute();

        model.addAttribute("events", events.getItems());
        return "calendar";
    }
}