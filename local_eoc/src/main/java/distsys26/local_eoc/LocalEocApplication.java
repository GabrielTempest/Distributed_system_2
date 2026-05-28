package distsys26.local_eoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LocalEocApplication {

	public static void main(String[] args) {
		SpringApplication.run(LocalEocApplication.class, args);
	}

}
