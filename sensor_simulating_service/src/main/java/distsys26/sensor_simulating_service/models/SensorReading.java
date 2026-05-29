package distsys26.sensor_simulating_service.models;

import distsys26.sensor_simulating_service.enums.SensorType;
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
