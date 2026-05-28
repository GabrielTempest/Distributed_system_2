package distsys26.early_warning_system.local_eoc;

import distsys26.early_warning_system.sensor_simulator.SensorReading;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/readings")
public class SensorController {

    @PostMapping
    public void receiveReading(@RequestBody SensorReading reading) {

        System.out.printf("ID: %s, type: %s, value: %f, unit: %s, timestamp: %s\n\n", 
                    reading.getSensorId(), 
                    reading.getSensorType(),
                    reading.getValue(),
                    reading.getUnit(),
                    reading.getTimestamp()
                );
    }
}