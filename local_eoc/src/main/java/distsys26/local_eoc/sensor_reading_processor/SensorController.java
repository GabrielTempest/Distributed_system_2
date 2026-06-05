package distsys26.local_eoc.sensor_reading_processor;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import distsys26.local_eoc.enums.SensorType;
import distsys26.local_eoc.sensor_reading_processor.models.SensorReading;

@RestController
@RequestMapping("/api/readings")
public class SensorController {

    public static final BlockingQueue<SensorReading> queue = new LinkedBlockingQueue<>(10000);

    private final LatestReadingStore store;

    public SensorController(LatestReadingStore store) {
        this.store = store;
    }

    @PostMapping
    public void receiveReading(@Valid @RequestBody SensorReading reading) {
        queue.offer(reading);
        store.update(reading);
    }

    /** Returns the latest received value for each sensor type as "value unit" strings. */
    @GetMapping("/latest")
    public Map<String, String> getLatestReadings() {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<SensorType, SensorReading> e : store.getAll().entrySet()) {
            SensorReading r = e.getValue();
            result.put(e.getKey().name(), String.format("%.2f %s", r.getValue(), r.getUnit()));
        }
        return result;
    }
}