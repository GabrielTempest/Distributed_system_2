package distsys26.local_eoc.sensor_reading_processor.disaster_alert_stamper;

import distsys26.local_eoc.enums.*;
import distsys26.local_eoc.sensor_reading_processor.models.*;

import java.util.List;

public class LandslideStamper extends Stamper {
    public LandslideStamper() {
        this.disasterType = DisasterType.LANDSLIDE;
    }

    /**
     * RED Alert immediately if: Rain OR Soil Moisture level 3
     */
    @Override
    protected boolean checkRedAlertConditions(List<SensorScore> measurements) {
        return (measurements.get(0) != null && measurements.get(0).getScore() == AlertLevel.RED.toInt()) ||
               (measurements.get(1) != null && measurements.get(1).getScore() == AlertLevel.RED.toInt());
    }

    /**
     * Alert level = 30% rain score + 40% moisture score + 15% tilt score + 15% vibration score.
     * Rescale to 100% if any of the relevant sensor scores are missing.
     */
    @Override
    protected AlertLevel calculateAlertScore(List<SensorScore> measurements) {
        int[] weights = new int[]{30, 40, 15, 15};
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
