package distsys26.local_eoc.sensor_reading_processor.disaster_alert_stamper;

import distsys26.local_eoc.enums.*;
import distsys26.local_eoc.sensor_reading_processor.models.*;

import java.util.List;

public class HeatwaveStamper extends Stamper {
    public HeatwaveStamper() {
        this.disasterType = DisasterType.HEATWAVE;
    }

    /**
     * RED Alert immediately if: Temperature above 40 degrees Celsius.
     */
    @Override
    protected boolean checkRedAlertConditions(List<SensorScore> measurements) {
        return measurements.get(0) != null && measurements.get(0).getValue() > 40;
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
        for (int i = 0; i < measurements.size(); i++) {
            if (measurements.get(i) != null) {
                totalScore += measurements.get(i).getScore() * weights[i];
                totalWeight += weights[i];
            }
        }
        return averageScoreToAlertLevel(totalScore / totalWeight);
    }
}
