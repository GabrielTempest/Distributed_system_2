package distsys26.sensor_simulating_service;

import java.util.HashMap;
import distsys26.sensor_simulating_service.enums.SensorType;
import distsys26.sensor_simulating_service.models.SensorValue;

/**
 * Mapping correlation between data for the fake sensors generator.
 */
public final class Mapping {
    /**
     * Mapping of SensorType to general values. 
     * Each SensorType is associated with a set of general min, max values and unit.
     */
    public static final HashMap<SensorType, SensorValue> sensorToGeneralValuesMap = new HashMap<>(){{
        put(SensorType.FLOODGAUGE, new SensorValue(0, 20, "m", "FG"));
        put(SensorType.RAINGAUGE, new SensorValue(0, 500, "mm", "RG"));
        put(SensorType.ANEMOMETER, new SensorValue(0, 60, "m/s", "ANM"));
        put(SensorType.BAROMETER, new SensorValue(300, 1100, "hPa", "BM"));
        put(SensorType.SOILMOISTURE, new SensorValue(0, 100, "%", "SM"));
        put(SensorType.TILT, new SensorValue(-90, 90, "degrees", "TL"));
        put(SensorType.VIBRATION, new SensorValue(0, 16, "g", "VBR"));
    }};
    
}