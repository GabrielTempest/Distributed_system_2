package distsys26.local_eoc.sensor_reading_processor;

import distsys26.local_eoc.sensor_reading_processor.models.SensorReading;
import distsys26.local_eoc.enums.SensorType;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class SensorProcessor {
    public SensorProcessor() {
    }

    public void process(List<SensorReading> batch) {
        List<SensorReading> averageReadings = new ArrayList<>();
        Map<SensorType, List<SensorReading>> grouped = batch.stream()
                .collect(Collectors.groupingBy(SensorReading::getSensorType));
        for (List<SensorReading> readings : grouped.values()) {
            double average = readings.stream()
                    .mapToDouble(SensorReading::getValue)
                    .average()
                    .orElse(0.0);
        }
    }
}
