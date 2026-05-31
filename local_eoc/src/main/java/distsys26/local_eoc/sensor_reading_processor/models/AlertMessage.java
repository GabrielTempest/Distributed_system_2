package distsys26.local_eoc.sensor_reading_processor.models;

import distsys26.local_eoc.enums.AlertLevel;
import distsys26.local_eoc.enums.DisasterType;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class AlertMessage {
    private String event_id;
    private String area_id;
    private String timestamp;
    private DisasterType disasterType;
    private AlertLevel alertLevel;
    private List<SensorScore> measurements;

    @Builder
    public AlertMessage(String timestamp, DisasterType disasterType, AlertLevel alertLevel, List<SensorScore> measurements) {
        this.event_id = UUID.randomUUID().toString();
        this.timestamp = timestamp;
        this.disasterType = disasterType;
        this.alertLevel = alertLevel;
        setMeasurements(measurements);
    }

    public void setMeasurements(List<SensorScore> measurements) {
        if (measurements == null) {
            this.measurements = new ArrayList<>();
            return;
        }
        List<SensorScore> temp = new ArrayList<>(measurements);
        temp.removeIf(score -> score == null);
        this.measurements = temp;
    }
}
