package distsys26.database_writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import distsys26.database_writer.db.DisasterAlertRepository;
import distsys26.database_writer.kafka.KafkaDTO;
import distsys26.database_writer.db.DBAlertEntity;

@Component
public class DBWriterConsumer {
    private final DisasterAlertRepository repository;
    private static final Logger log = LoggerFactory.getLogger(DBWriterConsumer.class);

    @Autowired
    public DBWriterConsumer(DisasterAlertRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "${eoc.alerts.topic}", groupId = "database-writer")
    public void consume(KafkaDTO kafkaDTO) {
        try {
            log.info("Received message: " + kafkaDTO.toString());

            // Map DTO to Entity
            DBAlertEntity alertEntity = DBAlertEntity.builder()
                    .alertId(kafkaDTO.getAlertId())
                    .localEocId(kafkaDTO.getLocalEocId())
                    .disasterType(kafkaDTO.getDisasterType())
                    .alertType(kafkaDTO.getAlertType())
                    .message(kafkaDTO.getMessage())
                    .timestamp(kafkaDTO.getTimestamp())
                    .build();

            // Save to database
            repository.save(alertEntity);

            // Log success
            log.info("Write successful.");
            
        } catch (Exception e) {
            log.error("Failed to process and save message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
