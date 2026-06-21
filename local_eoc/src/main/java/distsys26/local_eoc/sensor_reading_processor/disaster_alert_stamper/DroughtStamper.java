package distsys26.local_eoc.sensor_reading_processor.disaster_alert_stamper;

import distsys26.local_eoc.enums.*;
import distsys26.local_eoc.sensor_reading_processor.models.*;

import java.util.List;

public class DroughtStamper extends Stamper {
    public DroughtStamper() {
        this.disasterType = DisasterType.DROUGHT;
    }

    /**
     * RED Alert immediately if: Soil moisture below 20% and Raingauge level 1
     */
    @Override
    protected boolean checkRedAlertConditions(List<SensorScore> measurements) {
        return (measurements.get(0) != null && measurements.get(0).getValue() < 20)
                && (measurements.get(2) != null && measurements.get(2).getScore() == AlertLevel.GREEN.toInt());
    }

    /**
     * Alert level = 70% soil moisture score + 30% humidity score - RAINGAUGE ALERT LEVEL.
     * If raingauge alert level >= ORANGE, GREEN Alert
     * Rescale to 100% if any of the relevant sensor scores are missing.
     */
    @Override
    protected AlertLevel calculateAlertScore(List<SensorScore> measurements) {
        if (measurements.get(2) != null && measurements.get(2).getScore() >= AlertLevel.ORANGE.toInt()) {
            return AlertLevel.GREEN;
        }

        int[] weights = new int[]{70, 30, 0};
        double totalWeight = 0;
        double totalScore = 0;
        for (int i = 0; i < measurements.size(); i++) {
            if (measurements.get(i) != null) {
                totalScore += measurements.get(i).getScore() * weights[i];
                totalWeight += weights[i];
            }
        }
        return averageScoreToAlertLevel((totalScore / totalWeight) - measurements.get(2).getScore());
    }
}
