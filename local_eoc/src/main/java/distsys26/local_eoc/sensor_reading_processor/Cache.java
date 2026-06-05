package distsys26.local_eoc.sensor_reading_processor;

import distsys26.local_eoc.enums.SensorType;
import distsys26.local_eoc.sensor_reading_processor.models.SensorReading;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.Instant;

/**
 * Cache class that temporarily stores sensor readings to a queue and
 *      flushes them to returns as a batch for processing.
 */
@Service
public class Cache {
    public String last_flush_time;

    public Cache() {
        this.last_flush_time = Instant.now().toString();
    }

    /**
     * Flush sensor readings from the queue and return them as a batch. Then update the last flush time to the current time.
     * @return A list of SensorReading objects representing the flushed batch of sensor readings.
     */
    public List<SensorReading> flush() {
        this.last_flush_time = Instant.now().toString();
        List<SensorReading> batch = new ArrayList<>();
        SensorController.queue.drainTo(batch);
        return batch;
    }

    public void debugPrint(List<SensorReading> batch) {
        System.out.println("\nFlushed batch of size: " + batch.size());
        Map<SensorType, List<SensorReading>> grouped = batch.stream()
                .collect(Collectors.groupingBy(SensorReading::getSensorType));
        for (SensorType type : grouped.keySet()) {
            System.out.println("Sensor Type: " + type + ", Count: " + grouped.get(type).size());
        }
    }
}