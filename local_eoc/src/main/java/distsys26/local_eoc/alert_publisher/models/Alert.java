package distsys26.local_eoc.alert_publisher.models;

import distsys26.local_eoc.enums.*;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    private String alertId;
    private String localEocId;
    private DisasterType disasterType;
    // Field is named "alertType" so the JSON key matches the national_eoc consumer's field
    private AlertLevel alertType;
    private String message;
    private String timestamp;

    @Override
    public String toString() {
        return "Alert{alertId='" + alertId + "', localEocId='" + localEocId +
               "', disasterType=" + disasterType + ", alertType=" + alertType +
               ", message='" + message + "', timestamp='" + timestamp + "'}";
    }
}
