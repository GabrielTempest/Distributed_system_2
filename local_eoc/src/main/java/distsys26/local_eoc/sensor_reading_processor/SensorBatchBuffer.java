package distsys26.local_eoc.sensor_reading_processor;

import distsys26.local_eoc.sensor_reading_processor.models.SensorReading;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SensorBatchBuffer {

    private final SensorProcessor processor = new SensorProcessor();

    @Scheduled(fixedRate = 5000)
    public void flush() {
        List<SensorReading> batch = new ArrayList<>();
        SensorController.queue.drainTo(batch);
        if (!batch.isEmpty()) {
            processor.process(batch);
        }
        System.out.println("Flushed batch of size: " + batch.size());
    }
}