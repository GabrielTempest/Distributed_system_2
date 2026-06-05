package distsys26.local_eoc.sensor_reading_processor;

import distsys26.local_eoc.sensor_reading_processor.models.SensorReading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SensorBatchBuffer {
    private static final Logger log = LoggerFactory.getLogger(SensorBatchBuffer.class);

    private final SensorProcessor processor;

    public SensorBatchBuffer(SensorProcessor processor) {
        this.processor = processor;
    }

    @Scheduled(fixedRate = 5000)
    public void flush() {
        List<SensorReading> batch = new ArrayList<>();
        SensorController.queue.drainTo(batch);
        if (!batch.isEmpty()) {
            processor.process(batch);
        }
        log.debug("Flushed batch of size: {}", batch.size());
    }
}
