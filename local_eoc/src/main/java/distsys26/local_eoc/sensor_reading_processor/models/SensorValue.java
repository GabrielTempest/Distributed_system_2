package distsys26.local_eoc.sensor_reading_processor.models;

public class SensorValue {
    public double general_min;
    public double general_max;
    public double range;
    public String unit;
    public String sensorIdPrefix;

    public SensorValue(double general_min, double general_max, String unit, String sensorIdPrefix) {
        this.general_min = general_min;
        this.general_max = general_max;
        this.range = general_max - general_min;
        this.unit = unit;
        this.sensorIdPrefix = sensorIdPrefix;
    }
}
