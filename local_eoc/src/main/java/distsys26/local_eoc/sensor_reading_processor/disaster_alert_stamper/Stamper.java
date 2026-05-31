package distsys26.local_eoc.sensor_reading_processor.disaster_alert_stamper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import distsys26.local_eoc.enums.*;
import distsys26.local_eoc.sensor_reading_processor.Mapping;
import distsys26.local_eoc.sensor_reading_processor.models.AlertMessage;
import distsys26.local_eoc.sensor_reading_processor.models.SensorScore;

public abstract class Stamper {
    protected DisasterType disasterType;

    protected Stamper() {}

    protected abstract boolean checkRedAlertConditions(List<SensorScore> measurements);
    protected abstract AlertLevel calculateAlertScore(List<SensorScore> measurements);


    /**
     * Stamp alert message based on the evaluated sensor scores for the disaster type.
     * @param scores A HashMap mapping each SensorType to its corresponding SensorScore.
     * @param prebuildAlert a prebuild alert with disaster type and timestamp
     * @return an full alert, or null if all relevant sensor scores for the disaster type are not present
     */
    public AlertMessage stamp(HashMap<SensorType, SensorScore> scores, AlertMessage prebuildAlert) {
        // Get relevant sensor scores in order for alert message measurements
        List<SensorScore> measurements = getRelevantScoresInOrder(scores);
        if (measurements == null) {
            return null;
        }
        prebuildAlert.setMeasurements(measurements);
        // Check for RED alert conditions
        if (checkRedAlertConditions(measurements)) {
            prebuildAlert.setAlertLevel(AlertLevel.RED);
            return prebuildAlert;
        }
        // Otherwise, calculate alert level based on disaster-specific logic
        prebuildAlert.setAlertLevel(calculateAlertScore(measurements));
        return prebuildAlert;
    }


    /**
     * Helper method to convert average alert score to alert level based on predefined thresholds.
     * @param averageScore The average alert score calculated from the relevant sensor scores for a disaster type.
     * @return The corresponding AlertLevel based on the average alert score (0.8, 1.8, 2.5).
     */
    protected AlertLevel averageScoreToAlertLevel(double averageScore) {
        if (averageScore > 2.5) {
            return AlertLevel.RED;
        } else if (averageScore >= 1.8) {
            return AlertLevel.ORANGE;
        } else if (averageScore >= 0.8) {
            return AlertLevel.YELLOW;
        } else {
            return AlertLevel.GREEN;
        }
    }


    /**
     * Retrieve relevant sensor scores in order for alert message measurements based on the disaster type.
     * @param scores A HashMap mapping each SensorType to its corresponding SensorScore.
     * @return A list of SensorScore objects in the order defined by Mapping disasterToSensorMap. Or null if all relevant sensor scores for the disaster type are not present.
     */
    private List<SensorScore> getRelevantScoresInOrder(HashMap<SensorType, SensorScore> scores) {
        List<SensorType> relevantSensors = Mapping.disasterToSensorMap.get(disasterType);
        // Check if any relevant sensor scores for the disaster type are present
        boolean insufficient = true;
        for (SensorType sensorType : relevantSensors) {
            if (scores.containsKey(sensorType)) {
                insufficient = false;
                break;
            }
        }
        if (insufficient) {
            return null;
        }
        // Return relevant sensor scores in order for alert message measurements, with null for missing scores
        List<SensorScore> orderedScores = new ArrayList<>();
        for (SensorType sensorType : relevantSensors) {
            orderedScores.add(scores.get(sensorType));
        }
        return orderedScores;
    }
}
