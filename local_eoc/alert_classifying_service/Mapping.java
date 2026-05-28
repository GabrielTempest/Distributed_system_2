package distsys26.early_warning_system.local_eoc.alert_classifying_service;

import java.util.HashMap;
import java.util.List;

import distsys26.early_warning_system.dataclasses.enums.*;

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
    
}
