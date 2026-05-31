package distsys26.local_eoc.alert_publisher;

import distsys26.local_eoc.alert_publisher.models.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertPublisher {
    private static final Logger log = LoggerFactory.getLogger(AlertPublisher.class);

    private final KafkaTemplate<String, Alert> kafkaTemplate;

    @Value("${eoc.alerts.topic}")
    private String topic;

    public AlertPublisher(KafkaTemplate<String, Alert> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(Alert alert) {
        String key = alert.getDisasterType().name();
        kafkaTemplate.send(topic, key, alert)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish alert {} ({}:{}): {}",
                            alert.getAlertId(), alert.getDisasterType(), alert.getAlertType(),
                            ex.getMessage());
                } else {
                    log.info("Published alert {} [{}:{}] → partition {}",
                            alert.getAlertId(), alert.getDisasterType(), alert.getAlertType(),
                            result.getRecordMetadata().partition());
                }
            });
    }
}
