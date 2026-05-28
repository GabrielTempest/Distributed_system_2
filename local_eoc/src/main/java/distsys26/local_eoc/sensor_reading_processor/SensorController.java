package distsys26.local_eoc.sensor_reading_processor;

import org.springframework.web.bind.annotation.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import distsys26.local_eoc.sensor_reading_processor.models.SensorReading;

@RestController
@RequestMapping("/api/readings")
public class SensorController {
    public static final BlockingQueue<SensorReading> queue = new LinkedBlockingQueue<>(10000);

    @PostMapping
    public void receiveReading(@RequestBody SensorReading reading) {
        queue.offer(reading);
        System.out.printf("ID: %s, type: %s, value: %f, unit: %s, timestamp: %s\n\n", 
                    reading.getSensorId(), 
                    reading.getSensorType(),
                    reading.getValue(),
                    reading.getUnit(),
                    reading.getTimestamp()
                );
    }
}