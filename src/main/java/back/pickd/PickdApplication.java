package back.pickd;

import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAsync
public class PickdApplication {

	public static void main(String[] args) {
		SpringApplication.run(PickdApplication.class, args);
	}

}
