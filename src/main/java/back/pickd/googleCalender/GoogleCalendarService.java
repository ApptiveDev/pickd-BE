package back.pickd.googleCalender;

import com.google.api.client.util.DateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;

@Service
@Transactional(readOnly =true)
@RequiredArgsConstructor
public class GoogleCalendarService {

    private final Calendar calendar;

    public void testInsertEvent() throws IOException {
        // 1. 이벤트 객체 생성
        Event event = new Event()
                .setSummary("Spring Boot Google API Test")
                .setLocation("My Room")
                .setDescription("구글 캘린더 API 연동 테스트 중입니다.");

        // 2. 시작 시간 설정 (현재 시간)
        DateTime startDateTime = new DateTime(System.currentTimeMillis());
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Asia/Seoul");
        event.setStart(start);

        // 3. 종료 시간 설정 (1시간 뒤)
        DateTime endDateTime = new DateTime(System.currentTimeMillis() + 3600000);
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Seoul");
        event.setEnd(end);

        // 4. 캘린더에 삽입 ("primary"는 내 기본 캘린더)
        event = calendar.events().insert("primary", event).execute();

        System.out.println("✅ 일정 등록 성공!");
        System.out.println("🔗 일정 링크: " + event.getHtmlLink());
    }
}

