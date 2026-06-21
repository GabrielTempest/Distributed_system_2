package distsys26.national_eoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import distsys26.national_eoc.monitor_dashboard.MonitorDashboard;
import javafx.application.Application;

@SpringBootApplication
public class NationalEocApplication {

	public static void main(String[] args) {
		SpringApplication.run(NationalEocApplication.class, args).registerShutdownHook();;

		// 2. Start JavaFX on a separate thread
		Thread javafxThread = new Thread(() -> {
			try {
				Application.launch(MonitorDashboard.class, args);
			} catch (Exception e) {
				System.err.println("JavaFX failed to launch!");
			}
		});
		
		javafxThread.setDaemon(false); // Keeps JavaFX stable
		javafxThread.start();

		// 3. THE BRIDGE: Tell Spring to kill JavaFX when Spring terminates
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Backend is terminating... Shutting down JavaFX platform.");
			// Platform.exit() safely tears down the JavaFX thread structure
			javafx.application.Platform.exit();
    	}));
	}

}
