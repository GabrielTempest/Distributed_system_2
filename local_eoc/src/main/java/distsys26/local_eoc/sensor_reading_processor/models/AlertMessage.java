package distsys26.local_eoc.sensor_reading_processor.models;

import distsys26.local_eoc.enums.DisasterType;
import distsys26.local_eoc.enums.AlertLevel;
import java.util.List;

public class AlertMessage {
    private String event_id;
    private String area_id;
    private String timestamp;
    private DisasterType disasterType;
    private AlertLevel alertLevel;
    private List<Measurement> measurements;
}
