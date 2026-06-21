package distsys26.national_eoc.console_logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import distsys26.national_eoc.models.Alert;

@Component
public class LoggerConsumer {
    private static final Logger log = LoggerFactory.getLogger(LoggerConsumer.class);

    @KafkaListener(topics = "${eoc.alerts.topic}", groupId = "national-eoc-log")
    public void consume(Alert alert) {
        log.info(">>> ALERT RECEIVED from {}: [{} / {}] at {} — {}",
                alert.getLocalEocId(),
                alert.getDisasterType(),
                alert.getAlertType(),
                alert.getTimestamp(),
                alert.getMessage());
    }
}
