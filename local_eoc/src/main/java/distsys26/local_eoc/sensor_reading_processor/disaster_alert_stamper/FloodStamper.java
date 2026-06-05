package distsys26.local_eoc.sensor_reading_processor.disaster_alert_stamper;

import distsys26.local_eoc.enums.*;
import distsys26.local_eoc.sensor_reading_processor.models.*;

import java.util.List;

public class FloodStamper extends Stamper {
    public FloodStamper() {
        this.disasterType = DisasterType.FLOOD;
    }

    /**
     * No RED Alert conditions for flood, as flood alert level is determined by the average score of the relevant sensors.
     */
    @Override
    protected boolean checkRedAlertConditions(List<SensorScore> measurements) {
        return false;
    }

    /**
     * Alert level = max severity score among sensors (flood and rain gauge).
     */
    @Override
    protected AlertLevel calculateAlertScore(List<SensorScore> measurements) {
        int maxScore = 0;
        for (SensorScore score : measurements) {
            if (score != null && score.getScore() > maxScore) {
                maxScore = score.getScore();
            }
        }
        return AlertLevel.fromInt(maxScore);
    }
}
