package distsys26.local_eoc.sensor_reading_processor.disaster_alert_stamper;

import distsys26.local_eoc.enums.*;
import distsys26.local_eoc.sensor_reading_processor.models.*;

import java.util.List;

public class WildfireStamper extends Stamper {
    public WildfireStamper() {
        this.disasterType = DisasterType.WILDFIRE;
    }

    /**
     * RED Alert immediately if: Temperature level 3 and humidity below 20%
     */
    @Override
    protected boolean checkRedAlertConditions(List<SensorScore> measurements) {
        return (measurements.get(0) != null && measurements.get(0).getScore() == AlertLevel.RED.toInt())
                && (measurements.get(1) != null && measurements.get(1).getValue() < 20);
    }

    /**
     * Alert level = 40% temperature score + 40% humidity score + 20% raingauge score - RAINGAUGE ALERT LEVEL.
     * If raingauge alert level >= ORANGE, GREEN Alert
     * Rescale to 100% if any of the relevant sensor scores are missing.
     */
    @Override
    protected AlertLevel calculateAlertScore(List<SensorScore> measurements) {
        if (measurements.get(3) != null && measurements.get(3).getScore() >= AlertLevel.ORANGE.toInt()) {
            return AlertLevel.GREEN;
        }

        int[] weights = new int[]{40, 40, 20, 0};
        double totalWeight = 0;
        double totalScore = 0;
        for (int i = 0; i < measurements.size(); i++) {
            if (measurements.get(i) != null) {
                totalScore += measurements.get(i).getScore() * weights[i];
                totalWeight += weights[i];
            }
        }
        return averageScoreToAlertLevel((totalScore / totalWeight) - measurements.get(3).getScore());
    }
    
}
