package back.pickd;

import back.pickd.googleCalender.GoogleCalendarService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PickdApplication {

	public static void main(String[] args) {
		SpringApplication.run(PickdApplication.class, args);
	}

	@Bean
	public CommandLineRunner testCalendar(GoogleCalendarService calendarService) {
		return args -> {
			try {
				calendarService.testInsertEvent();
			} catch (Exception e) {
				System.err.println("❌ 테스트 실패: " + e.getMessage());
				e.printStackTrace();
			}
		};
	}
}
