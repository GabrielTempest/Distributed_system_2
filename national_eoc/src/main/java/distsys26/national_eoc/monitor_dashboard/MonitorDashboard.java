package distsys26.national_eoc.monitor_dashboard;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.stage.WindowEvent;

public class MonitorDashboard extends Application {
    public static AlertTable alertTable = new AlertTable();

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Crucial: Prevent JavaFX from shutting down implicitly if the window hides
        Platform.setImplicitExit(false);

        // 2. Intercept and disable the manual close ("X" button / Alt+F4)
        primaryStage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
            System.out.println("Close button clicked, but closing is disabled!");
            event.consume(); // Consuming the event stops the window from closing
        });

        primaryStage.setTitle("Alert Monitor Dashboard");
        primaryStage.setWidth(600);
        primaryStage.setHeight(400);
        primaryStage.setScene(new Scene(alertTable));
        primaryStage.show();

        // 3. Catch crashes specific to the JavaFX Application Thread
        setupCrashHandler();
    }

    @Override
    public void stop() throws Exception {
        // Optional: Decide what happens when the UI is closed.
        // If you want the whole backend to stop when the UI closes, uncomment this:
        // MyMultiPurposeApplication.springContext.close();
    }

    private void setupCrashHandler() {
        // If an unexpected runtime exception happens inside the UI thread,
        // this catches it, logs it, and prevents it from bubbling up to kill the JVM.
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("CRITICAL UI CRASH DETECTED in " + thread.getName());
            throwable.printStackTrace();
            // The UI might freeze or look broken, but your Spring Boot backend 
            // running on the main/web threads remains 100% operational.
        });
    }
    
}
