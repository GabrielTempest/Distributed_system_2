package distsys26.local_eoc.sensor_reading_processor.disaster_alert_stamper;

import distsys26.local_eoc.enums.*;
import distsys26.local_eoc.sensor_reading_processor.models.*;

import java.util.List;

public class ColdspellStamper extends Stamper {
    public ColdspellStamper() {
        this.disasterType = DisasterType.COLDSPELL;
    }

    /**
     * RED Alert immediately if: Temperature below -10 degrees Celsius.
     */
    @Override
    protected boolean checkRedAlertConditions(List<SensorScore> measurements) {
        return measurements.get(0) != null && measurements.get(0).getValue() < -10;
    }

    /**
     * Alert level = 60% temperature score + 20% humidity score + 20% anemometer score.
     * Rescale to 100% if any of the relevant sensor scores are missing.
     */
    @Override
    protected AlertLevel calculateAlertScore(List<SensorScore> measurements) {
        int[] weights = new int[]{60, 20, 20};
        double totalWeight = 0;
        double totalScore = 0;

        AlertLevel coldTemperatureScore;
        Double temperatureValue = measurements.get(0) != null ? measurements.get(0).getValue() : null;
        if (temperatureValue != null) {
            if (temperatureValue < -15) {
                coldTemperatureScore = AlertLevel.RED;
            } else if (temperatureValue < -5) {
                coldTemperatureScore = AlertLevel.ORANGE;
            } else if (temperatureValue < 5) {
                coldTemperatureScore = AlertLevel.YELLOW;
            } else {
                coldTemperatureScore = AlertLevel.GREEN;
            }
            totalWeight = weights[0];
            totalScore = weights[0] * coldTemperatureScore.toInt();
        }

        for (int i = 1; i < measurements.size(); i++) {
            if (measurements.get(i) != null) {
                totalScore += measurements.get(i).getScore() * weights[i];
                totalWeight += weights[i];
            }
        }
        return averageScoreToAlertLevel(totalScore / totalWeight);
    }
    
}
