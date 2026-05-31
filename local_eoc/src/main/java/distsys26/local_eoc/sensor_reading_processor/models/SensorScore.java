package distsys26.local_eoc.sensor_reading_processor.models;

import distsys26.local_eoc.enums.SensorType;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorScore {
    @NotNull(message = "sensorType is required")
    private SensorType sensorType;

    @NotNull(message = "value is required")
    private Double value;

    @NotBlank(message = "unit is required")
    private String unit;

    @NotNull(message = "score is required")
    private int score;
}
