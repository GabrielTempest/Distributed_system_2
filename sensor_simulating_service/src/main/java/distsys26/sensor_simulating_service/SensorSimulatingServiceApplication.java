package distsys26.sensor_simulating_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SensorSimulatingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SensorSimulatingServiceApplication.class, args);
	}

}
