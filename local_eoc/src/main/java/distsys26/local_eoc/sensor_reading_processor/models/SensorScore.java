package distsys26.local_eoc.sensor_reading_processor.models;

import distsys26.local_eoc.enums.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private int score;

    @Override
    public String toString() {
        return "SensorScore{" +
                "sensorType=" + sensorType +
                ", value=" + value +
                ", unit='" + unit + '\'' +
                ", score=" + AlertLevel.fromInt(score).name() +
                '}';
    }
}
