package distsys26.database_writer.db;

import distsys26.database_writer.enums.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "disaster_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DBAlertEntity {

    @Id
    private String alertId; // Using the incoming alertId as the Primary Key
    
    private String localEocId;
    
    @Enumerated(EnumType.STRING)
    private DisasterType disasterType;
    
    @Enumerated(EnumType.STRING)
    private AlertType alertType;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    private String timestamp;
}