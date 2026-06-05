package distsys26.local_eoc.sensor_reading_processor;

import distsys26.local_eoc.enums.*;
import distsys26.local_eoc.sensor_reading_processor.models.*;
import distsys26.local_eoc.sensor_reading_processor.disaster_alert_stamper.*;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public final class AlertStamper {
    private AlertStamper() {}

    /**
     * Stamp alert messages based on the evaluated sensor scores for each disaster type.
     * @param scores A HashMap mapping each SensorType to its corresponding SensorScore.
     * @return A list of AlertMessage objects containing the details of the alerts to be sent to the EOC.
     */
    public static List<AlertData> stamp(HashMap<SensorType, SensorScore> scores) {
        List<AlertData> alerts = new ArrayList<>();
        AlertData alert;
        for (DisasterType disasterType : DisasterType.values()) {
            alert = stampAlert(scores, disasterType);
            if (alert != null) {
                alerts.add(alert);
            }
        }
        return alerts;
    }


    
    /**
     * Helper method to switch between different alert stamping logic based on the disaster type.
     * If all relevant sensor scores for the disaster type are not present, returns null to indicate insufficient data for stamping an alert.
     * @param scores A HashMap mapping each SensorType to its corresponding SensorScore.
     * @param disasterType The type of disaster to determine the stamping logic.
     * @return An AlertMessage object containing the details of the alert to be sent to the EOC.
     */
    private static AlertData stampAlert(HashMap<SensorType, SensorScore> scores, 
                                            DisasterType disasterType
                                            ) {
        // Check if the disaster type is supported
        if (!Mapping.disasterToSensorMap.containsKey(disasterType)) {
            return null;
        }

        // Prebuild alert message with the disaster type, measurements and alert level will be set in the stamper
        AlertData prebuildAlert = AlertData.builder()
                .disasterType(disasterType)
                .build();

        // Switch between different alert stamping logic based on the disaster type
        Stamper stamper = null;
        switch (disasterType) {
            case FLOOD:
                stamper = new FloodStamper();
                break;
            case TYPHOON:
                stamper = new TyphoonStamper();
                break;
            case LANDSLIDE:
                stamper = new LandslideStamper();
                break;
        }
        return stamper == null ? null : stamper.stamp(scores, prebuildAlert);
    }
}
