package distsys26.national_eoc.models;

import distsys26.national_eoc.enums.AlertType;
import distsys26.national_eoc.enums.DisasterType;

import lombok.*;

@Data
@NoArgsConstructor
public class Alert {
    private String alertId;
    private String localEocId;
    private DisasterType disasterType;
    private AlertType alertType;
    private String message;
    private String timestamp;

    @Override
    public String toString() {
        return "Alert{alertId='" + alertId + "', localEocId='" + localEocId +
               "', disasterType=" + disasterType + ", alertType=" + alertType +
               ", message='" + message + "', timestamp='" + timestamp + "'}";
    }
}
