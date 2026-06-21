package distsys26.local_eoc.sensor_reading_processor.disaster_alert_stamper;

import distsys26.local_eoc.enums.*;
import distsys26.local_eoc.sensor_reading_processor.models.*;

import java.util.List;

public class EarthquakeStamper extends Stamper {
    public EarthquakeStamper() {
        this.disasterType = DisasterType.EARTHQUAKE;
    }

    /**
     * RED Alert immediately if: none, as earthquake alert is based solely on vibration score.
     */
    @Override
    protected boolean checkRedAlertConditions(List<SensorScore> measurements) {
        return false;
    }

    /**
     * Alert level = 100% vibration score.
     */
    @Override
    protected AlertLevel calculateAlertScore(List<SensorScore> measurements) {
        return averageScoreToAlertLevel(measurements.get(0).getScore());
    }
    
}
