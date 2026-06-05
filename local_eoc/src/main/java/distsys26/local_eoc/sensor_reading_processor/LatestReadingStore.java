package distsys26.local_eoc.sensor_reading_processor;

import distsys26.local_eoc.enums.SensorType;
import distsys26.local_eoc.sensor_reading_processor.models.SensorReading;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LatestReadingStore {

    private final Map<SensorType, SensorReading> latest = new ConcurrentHashMap<>();

    public void update(SensorReading reading) {
        latest.put(reading.getSensorType(), reading);
    }

    public Map<SensorType, SensorReading> getAll() {
        return Collections.unmodifiableMap(latest);
    }
}
