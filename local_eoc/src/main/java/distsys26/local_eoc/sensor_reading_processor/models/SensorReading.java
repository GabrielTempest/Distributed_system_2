package distsys26.local_eoc.sensor_reading_processor.models;

import distsys26.local_eoc.enums.SensorType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorReading {

    private String sensorId;

    @NonNull
    private SensorType sensorType;

    private double value;

    @NonNull
    private String unit;

    private String timestamp;
    
}
