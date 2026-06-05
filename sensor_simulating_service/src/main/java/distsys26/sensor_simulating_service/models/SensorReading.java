package distsys26.sensor_simulating_service.models;

import distsys26.sensor_simulating_service.enums.SensorType;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorReading {
    @NotBlank(message = "sensorId is required")
    private String sensorId;

    @NotNull(message = "sensorType is required")
    private SensorType sensorType;

    @NotNull(message = "value is required")
    private Double value;

    @NotBlank(message = "unit is required")
    private String unit;

    @NotBlank(message = "timestamp is required")
    private String timestamp;
}
