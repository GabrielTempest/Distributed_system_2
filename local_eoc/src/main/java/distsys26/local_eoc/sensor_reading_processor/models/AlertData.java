package distsys26.local_eoc.sensor_reading_processor.models;

import distsys26.local_eoc.enums.AlertLevel;
import distsys26.local_eoc.enums.DisasterType;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AlertData {
    private DisasterType disasterType;
    private AlertLevel alertLevel;
    private List<SensorScore> measurements;

    @Builder
    public AlertData(DisasterType disasterType, AlertLevel alertLevel, List<SensorScore> measurements) {
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

    public String measurementsToString() {
        StringBuilder sb = new StringBuilder();
        for (SensorScore score : measurements) {
            sb.append(score.toString());
            if (measurements.indexOf(score) != measurements.size() - 1) {
                sb.append("; ");
            }
        }
        return sb.toString();
    }
}
