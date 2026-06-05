package distsys26.national_eoc.alert_consumer;

import distsys26.national_eoc.alert_consumer.models.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UIConsumer {
    private static final Logger log = LoggerFactory.getLogger(UIConsumer.class);

    @KafkaListener(topics = "${eoc.alerts.topic}", groupId = "national-eoc-ui")
    public void consume(Alert alert) {
        log.info(">>> ALERT RECEIVED from {}: [{} / {}] at {} — {}",
                alert.getLocalEocId(),
                alert.getDisasterType(),
                alert.getAlertType(),
                alert.getTimestamp(),
                alert.getMessage());
    }
}
