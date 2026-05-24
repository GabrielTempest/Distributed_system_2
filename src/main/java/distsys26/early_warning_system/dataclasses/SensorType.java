package distsys26.early_warning_system.dataclasses;

/**
 * Enumeration for the different types of sensors that can be used in the early warning system. 
 * Each sensor type corresponds to a specific type of measurement that can be taken to monitor for potential disasters.
 */
public enum SensorType {
    BAROMETER,
    ANEMOMETER,
    RAINGAUGE,
    FLOODGAUGE,
    VIBRATION,
    TILT,
    SOILMOISTURE    
}
