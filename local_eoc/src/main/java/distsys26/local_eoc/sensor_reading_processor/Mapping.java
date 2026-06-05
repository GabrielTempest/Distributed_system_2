package distsys26.local_eoc.sensor_reading_processor;

import java.util.HashMap;
import java.util.List;

import distsys26.local_eoc.enums.*;

/**
 * Mapping correlation between data for the alert classifier.
 */
public final class Mapping {

    /**
     * Mapping of DisasterType to SensorType. 
     * Each DisasterType is associated with a list of SensorTypes that are relevant for monitoring that type of disaster.
     */
    public static final HashMap<DisasterType, List<SensorType>> disasterToSensorMap = new HashMap<>(){{
        put(DisasterType.FLOOD, List.of(
            SensorType.FLOODGAUGE, SensorType.RAINGAUGE
        ));
        put(DisasterType.TYPHOON, List.of(
            SensorType.RAINGAUGE, SensorType.ANEMOMETER, SensorType.BAROMETER
        ));
        put(DisasterType.LANDSLIDE, List.of(
            SensorType.RAINGAUGE, SensorType.SOILMOISTURE, SensorType.TILT, SensorType.VIBRATION
        ));
    }};

    /**
     * Mapping of SensorType to threshold values.
     * Each SensorType is associated with an array of threshold values that determine the alert level based on the sensor reading.
     */
    public static final HashMap<SensorType, int[]> sensorReadingThresholds = new HashMap<>(){{
        put(SensorType.FLOODGAUGE, new int[]{3, 6, 12});
        put(SensorType.RAINGAUGE, new int[]{100, 220, 350});
        put(SensorType.ANEMOMETER, new int[]{15, 30, 40});
        put(SensorType.BAROMETER, new int[]{950, 800, 600});
        put(SensorType.SOILMOISTURE, new int[]{40, 60, 80});
        put(SensorType.TILT, new int[]{10, 30, 50});
        put(SensorType.VIBRATION, new int[]{2, 6, 10});
    }};
    
}
