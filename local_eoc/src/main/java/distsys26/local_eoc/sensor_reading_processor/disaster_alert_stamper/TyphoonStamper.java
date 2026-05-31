package distsys26.local_eoc.sensor_reading_processor.disaster_alert_stamper;

import distsys26.local_eoc.enums.*;
import distsys26.local_eoc.sensor_reading_processor.models.*;

import java.util.List;

public class TyphoonStamper extends Stamper {
    public TyphoonStamper() {
        this.disasterType = DisasterType.TYPHOON;
    }

    /**
     * RED Alert immediately if: Air pressure level 3
     */
    @Override
    protected boolean checkRedAlertConditions(List<SensorScore> measurements) {
        return measurements.get(2) != null && measurements.get(2).getScore() == AlertLevel.RED.toInt();
    }

    /**
     * Alert level = 45% air pressure score + 35% wind speed score + 20% rainfall score.
     * Rescale to 100% if any of the relevant sensor scores are missing.
     */
    @Override
    protected AlertLevel calculateAlertScore(List<SensorScore> measurements) {
        int[] weights = new int[]{45, 35, 20};
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
