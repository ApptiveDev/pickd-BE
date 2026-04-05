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

}
