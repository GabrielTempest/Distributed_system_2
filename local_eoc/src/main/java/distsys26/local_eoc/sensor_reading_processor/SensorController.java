package distsys26.local_eoc.sensor_reading_processor;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import distsys26.local_eoc.sensor_reading_processor.models.SensorReading;

/**
 * Controller to receive sensor readings via REST API and enqueue them for batch processing.
 */
@RestController
@RequestMapping("/api/readings")
public class SensorController {
    /**
     * Thread-safe queue to hold incoming sensor readings until they are processed in batches.
     */
    public static final BlockingQueue<SensorReading> queue = new LinkedBlockingQueue<>(10000);

    @PostMapping
    public void receiveReading(@Valid @RequestBody SensorReading reading) {
        queue.offer(reading);
    }
}