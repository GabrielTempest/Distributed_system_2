package distsys26.database_writer.kafka;

import distsys26.database_writer.enums.*;

import lombok.*;

@Data
@NoArgsConstructor
public class KafkaDTO {
    private String alertId;
    private String localEocId;
    private DisasterType disasterType;
    private AlertType alertType;
    private String message;
    private String timestamp;

    @Override
    public String toString() {
        return "KafkaDTO{alertId='" + alertId + "', localEocId='" + localEocId +
               "', disasterType=" + disasterType + ", alertType=" + alertType +
               ", message='" + message + "', timestamp='" + timestamp + "'}";
    }
}
