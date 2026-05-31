package distsys26.local_eoc.sensor_reading_processor.models;

import distsys26.local_eoc.enums.SensorType;
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
