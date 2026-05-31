package distsys26.local_eoc.sensor_reading_processor;

import distsys26.local_eoc.sensor_reading_processor.models.*;
import java.util.List;

public class ResultGenerator {
    private Cache cache;

    public ResultGenerator() {
        this.cache = new Cache();
    }

    public void generate() {
        List<SensorReading> batch = cache.flush();
        String timestamp = cache.last_flush_time;
        if (batch.isEmpty()) {
            System.out.println("No sensor readings to process at " + timestamp);
            return;
        }

        List<AlertMessage> alerts = AlertStamper.stamp(SensorProcessor.processBatch(batch), timestamp);
        debugPrint(alerts);
    }

    private void debugPrint(List<AlertMessage> alerts) {
        System.out.println("\nGenerated alerts: " + alerts.size());
        for (AlertMessage alert : alerts) {
            System.out.println("Alert: " + alert.getEvent_id() +
                    "\nArea: " + alert.getArea_id() +
                    "\nTimestamp: " + alert.getTimestamp() +
                    "\nDisaster: " + alert.getDisasterType() +
                    "\nLevel: " + alert.getAlertLevel()
            );
            for (SensorScore score : alert.getMeasurements()) {
                System.out.println("    Sensor: " + score.getSensorType() +
                        ", Value: " + score.getValue() +
                        " Unit: " + score.getUnit() +
                        ", Score: " + score.getScore()
                );
            }
        }
    }
}
