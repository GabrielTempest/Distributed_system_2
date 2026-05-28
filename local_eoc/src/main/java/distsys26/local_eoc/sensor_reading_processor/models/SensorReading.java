package distsys26.local_eoc.sensor_reading_processor.models;

import distsys26.local_eoc.enums.SensorType;

public class SensorReading {
    private String sensorId;
    private SensorType sensorType;
    private double value;
    private String unit;
    private String timestamp;

    public SensorReading() {}

    public SensorReading(
            String sensorId,
            SensorType sensorType,
            double value,
            String unit,
            String timestamp) {

        this.sensorId = sensorId;
        this.sensorType = sensorType;
        this.value = value;
        this.unit = unit;
        this.timestamp = timestamp;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
