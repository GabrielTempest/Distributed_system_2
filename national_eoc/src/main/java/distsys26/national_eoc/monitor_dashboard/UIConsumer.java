package distsys26.national_eoc.monitor_dashboard;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import distsys26.national_eoc.models.Alert;
import javafx.application.Platform;

@Component
public class UIConsumer {

    @KafkaListener(topics = "${eoc.alerts.topic}", groupId = "national-eoc-ui")
    public void consume(Alert alert) {
        Platform.runLater(() -> {
            MonitorDashboard.alertTable.receiveAlertMessage(alert);
        });
    }
}
