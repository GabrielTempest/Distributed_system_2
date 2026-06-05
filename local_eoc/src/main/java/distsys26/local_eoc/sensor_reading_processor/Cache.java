package distsys26.local_eoc.sensor_reading_processor;

import distsys26.local_eoc.enums.AlertLevel;
import distsys26.local_eoc.enums.DisasterType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class Cache {
    private final Map<DisasterType, AlertLevel> lastPublished = new EnumMap<>(DisasterType.class);

    public AlertLevel getLastPublished(DisasterType type) {
        return lastPublished.getOrDefault(type, AlertLevel.GREEN);
    }

    public void setLastPublished(DisasterType type, AlertLevel level) {
        lastPublished.put(type, level);
    }
}
