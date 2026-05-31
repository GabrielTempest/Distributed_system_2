package distsys26.local_eoc.sensor_reading_processor;

import distsys26.local_eoc.enums.AlertType;
import distsys26.local_eoc.enums.DisasterType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class Cache {
    private final Map<DisasterType, AlertType> lastPublished = new EnumMap<>(DisasterType.class);

    public AlertType getLastPublished(DisasterType type) {
        return lastPublished.getOrDefault(type, AlertType.GREEN);
    }

    public void setLastPublished(DisasterType type, AlertType level) {
        lastPublished.put(type, level);
    }
}
