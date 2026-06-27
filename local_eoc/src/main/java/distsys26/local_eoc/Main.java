package distsys26.local_eoc;

import distsys26.local_eoc.alert_publisher.AlertPublisher;
import distsys26.local_eoc.alert_publisher.models.Alert;
import distsys26.local_eoc.sensor_reading_processor.models.*;
import distsys26.local_eoc.sensor_reading_processor.ResultGenerator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
class Main {
    private AlertPublisher alertPublisher;
    private String localEocId;
    private ResultGenerator resultGenerator;

    public Main(AlertPublisher alertPublisher,
                           @Value("${eoc.local.id}") String localEocId,
                           ResultGenerator resultGenerator
                        ) {
        this.alertPublisher = alertPublisher;
        this.localEocId = localEocId;
        this.resultGenerator = resultGenerator;
    }

    @Scheduled(fixedRate = 5000)
    public void run() {
        List<AlertData> alertDataList = resultGenerator.generate();
        String timestamp = resultGenerator.getLastFlushTime();

        // If no alerts were generated, log the timestamp and return early
        if (alertDataList.isEmpty()) {
            System.out.println("No alerts generated at " + timestamp);
            return;
        }

        // An additional loop to increase the number of alerts generated for testing purposes
        for(int i = 0; i < 1; i++) {
            // Else, publish the generated alerts
            for (AlertData alertData : alertDataList) {
                Alert alert = new Alert(
                        UUID.randomUUID().toString(),
                        localEocId,
                        alertData.getDisasterType(),
                        alertData.getAlertLevel(),
                        alertData.measurementsToString(),
                        timestamp
                );
                alertPublisher.publish(alert);
            }
        }
    }
}
