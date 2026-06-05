package distsys26.local_eoc.sensor_simulator;

/** Min/max range, unit and ID prefix for one sensor type. */
public record SensorMeta(double min, double max, String unit, String prefix) {}
