package distsys26.local_eoc.config;

// Superseded by Spring Boot auto-configuration driven by spring.kafka.* in application.properties.
// The manual ProducerFactory/KafkaTemplate approach here would bypass spring.json.add.type.headers=false,
// breaking cross-app JSON deserialization in the national_eoc consumer.
class KafkaProducerConfig {
}
