package distsys26.local_eoc.alert_publisher.models;

import distsys26.local_eoc.enums.AlertType;
import distsys26.local_eoc.enums.DisasterType;

public class Alert {
    private String alertId;
    private String localEocId;
    private DisasterType disasterType;
    private AlertType alertType;
    private String message;
    private String timestamp;

    public Alert() {}

    public Alert(String alertId, String localEocId, DisasterType disasterType,
                 AlertType alertType, String message, String timestamp) {
        this.alertId = alertId;
        this.localEocId = localEocId;
        this.disasterType = disasterType;
        this.alertType = alertType;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }

    public String getLocalEocId() { return localEocId; }
    public void setLocalEocId(String localEocId) { this.localEocId = localEocId; }

    public DisasterType getDisasterType() { return disasterType; }
    public void setDisasterType(DisasterType disasterType) { this.disasterType = disasterType; }

    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "Alert{alertId='" + alertId + "', localEocId='" + localEocId +
               "', disasterType=" + disasterType + ", alertType=" + alertType +
               ", message='" + message + "', timestamp='" + timestamp + "'}";
    }
}
