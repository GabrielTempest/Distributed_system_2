package distsys26.national_eoc.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer configuration class that sets up the necessary properties for consuming messages from Kafka topics.
 * It uses Spring's @Configuration and @EnableKafka annotations to enable Kafka support in the application.
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    /**
     * The bootstrap servers for the Kafka consumer.
     */
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * The group ID for the Kafka consumer.
     */
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * Creates a ConsumerFactory bean that configures the Kafka consumer with the necessary properties.
     * @return a ConsumerFactory instance configured for consuming messages from Kafka topics.
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
}
