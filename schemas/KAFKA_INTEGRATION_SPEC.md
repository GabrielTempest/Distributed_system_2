# Kafka Integration Specification: AlertMessage Events

## 1. Message Contract Overview

**Message Type:** `AlertMessage`  
**Serialization Format:** JSON  
**Schema Version:** 1.0  
**Documentation:** See `AlertMessage.schema.json`

---

## 2. Kafka Topic Configuration

### Topic Name and Partitioning

```yaml
topic_name: "disaster.alerts"
key_format: "area_id"  # e.g., "HCM", "HANOI"
partitions: 6           # One per major region/area for parallel processing
replication_factor: 2   # Minimum for production resilience
```

**Rationale:**

- **Key Strategy (`area_id`)**: Ensures all alerts from the same region go to the same partition → preserves order by area
- **Partition Count**: Scale with number of geographic areas; 6 partitions supports ~6 concurrent national EOC consumers
- **Replication Factor**: 2 provides fault tolerance; if one broker fails, the other replica takes over

### Topic Retention Policy

```yaml
retention.ms: 604800000  # 7 days
retention.bytes: -1      # No size limit
cleanup.policy: "delete" # Age-off old alerts after retention window
```

**Rationale:** Disaster alerts have compliance/audit requirements (keep for 7 days), but don't need indefinite storage.

---

## 3. Message Key Format

**Key:** `area_id` (String)  
**Examples:**

```terminal
Key: "HCM"
Key: "HANOI"
Key: "DANANG"
```

**Why `area_id` as key:**

- ✅ Ensures topic ordering per area (all HCM alerts stay together)
- ✅ National EOC can subscribe to specific areas via consumer groups/filtering
- ✅ Enables stateful processing per region in stream processors

---

## 4. Field Validation Rules

| Field | Type | Required | Constraints | Example |
|-------|------|----------|-------------|---------|
| `event_id` | String (UUID) | ✅ Yes | Must be valid UUID v4 | `550e8400-e29b-41d4-a716-446655440000` |
| `area_id` | String | ✅ Yes | Uppercase, 2-10 chars, letters only | `HCM`, `HANOI` |
| `timestamp` | String (ISO 8601) | ✅ Yes | Format: `YYYY-MM-DDTHH:MM:SSZ` | `2026-05-31T14:30:00Z` |
| `disaster_type` | Enum | ✅ Yes | One of: FLOOD, TYPHOON, HEATWAVE, LANDSLIDE | `FLOOD` |
| `alert_level` | Enum | ✅ Yes | One of: GREEN, YELLOW, ORANGE, RED | `RED` |
| `measurements` | Array | ✅ Yes | Minimum 1 item, no nulls | `[{...}]` |
| `measurements[].sensor_type` | Enum | ✅ Yes | WATER_LEVEL, WIND_SPEED, TEMPERATURE, RAINFALL, GROUND_MOVEMENT, HUMIDITY | `WATER_LEVEL` |
| `measurements[].value` | Number | ✅ Yes | Any numeric value (positive or negative) | `2.8` |
| `measurements[].unit` | Enum | ✅ Yes | mm, cm, m, km/h, °C, °F, %, mm/h | `m` |
| `measurements[].confidence` | Number | ✅ Yes | Range: 0.0–1.0 (inclusive) | `0.95` |

### Validation Rules

1. **UUID Format**: `event_id` must pass UUID v4 validation

   ```java
   // Example validation
   UUID.fromString(alertMessage.getEventId()); // Throws if invalid
   ```

2. **Area ID Pattern**: Must match `^[A-Z]{2,}$` (uppercase letters only)

   ```ter
   ✅ Valid: HCM, HANOI, DANANG, NY
   ❌ Invalid: hcm, Ho_Chi_Minh, 123, HC
   ```

3. **Timestamp Format**: Must be ISO 8601 with UTC timezone

   ```t
   ✅ Valid: 2026-05-31T14:30:00Z
   ❌ Invalid: 2026-05-31 14:30:00, 1622476200000, 05-31-2026
   ```

4. **Confidence Range**: Strictly between 0.0 and 1.0

   ```t
   ✅ Valid: 0.0, 0.5, 0.95, 1.0
   ❌ Invalid: -0.1, 1.5, 2.0
   ```

5. **Non-Empty Measurements**: At least one measurement must be present

   ```t
   ✅ Valid: measurements: [{...}]
   ❌ Invalid: measurements: [], measurements: null
   ```

---

## 5. Spring Boot Producer Implementation

### Updated AlertMessage Model

```java
package distsys26.local_eoc.sensor_reading_processor.models;

import distsys26.local_eoc.enums.DisasterType;
import distsys26.local_eoc.enums.AlertLevel;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertMessage {
    
    @JsonProperty("event_id")
    @NotBlank(message = "event_id is required")
    private String eventId;
    
    @JsonProperty("area_id")
    @NotBlank(message = "area_id is required")
    @Pattern(regexp = "^[A-Z]{2,}$", message = "area_id must be uppercase letters, 2-10 chars")
    @Size(min = 2, max = 10)
    private String areaId;
    
    @NotNull(message = "timestamp is required")
    private String timestamp;
    
    @JsonProperty("disaster_type")
    @NotNull(message = "disaster_type is required")
    private DisasterType disasterType;
    
    @JsonProperty("alert_level")
    @NotNull(message = "alert_level is required")
    private AlertLevel alertLevel;
    
    @NotEmpty(message = "measurements cannot be empty")
    @Size(min = 1, message = "At least one measurement is required")
    private List<Measurement> measurements;
    
    @PrePersist
    public void generateDefaults() {
        if (this.eventId == null) {
            this.eventId = UUID.randomUUID().toString();
        }
        if (this.timestamp == null) {
            this.timestamp = Instant.now().toString();
        }
    }
}
```

### Measurement Model

```java
package distsys26.local_eoc.sensor_reading_processor.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {
    
    @JsonProperty("sensor_type")
    @NotNull(message = "sensor_type is required")
    private String sensorType;
    
    @NotNull(message = "value is required")
    private Double value;
    
    @NotBlank(message = "unit is required")
    private String unit;
    
    @NotNull(message = "confidence is required")
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double confidence;
}
```

### Kafka Producer Service

```java
package distsys26.local_eoc.kafka;

import distsys26.local_eoc.sensor_reading_processor.models.AlertMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import jakarta.validation.Valid;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertProducer {
    
    private final KafkaTemplate<String, AlertMessage> kafkaTemplate;
    private static final String TOPIC = "disaster.alerts";
    
    /**
     * Sends an alert message to the Kafka topic.
     * 
     * @param alert The alert message to send (validated)
     */
    public void sendAlert(@Valid AlertMessage alert) {
        String key = alert.getAreaId(); // Partition key
        
        Message<AlertMessage> message = MessageBuilder
            .withPayload(alert)
            .setHeader(KafkaHeaders.TOPIC, TOPIC)
            .setHeader(KafkaHeaders.MESSAGE_KEY, key)
            .setHeader("schema_version", "1.0")
            .build();
        
        kafkaTemplate.send(message)
            .whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error("Failed to send alert event_id={} to topic {}", 
                        alert.getEventId(), TOPIC, exception);
                } else {
                    log.info("Alert sent successfully. event_id={}, partition={}, offset={}", 
                        alert.getEventId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            });
    }
}
```

### application.yml Configuration

```yaml
spring:
  application:
    name: local_eoc
  
  kafka:
    bootstrap-servers: localhost:9092
    
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all                      # Wait for all replicas to acknowledge
      retries: 3
      properties:
        linger.ms: 10               # Batch messages for 10ms before sending
        batch.size: 16384           # Send when batch reaches 16KB
        enable.idempotence: true    # Exactly-once semantics
    
    producer-config:
      max.in.flight.requests.per.connection: 5
```

### Usage Example in Controller

```java
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {
    
    private final AlertProducer alertProducer;
    
    @PostMapping
    public ResponseEntity<AlertMessage> createAlert(@Valid @RequestBody AlertMessage alert) {
        alertProducer.sendAlert(alert);
        return ResponseEntity.accepted().body(alert);
    }
}
```

---

## 6. Versioning Strategy

### Schema Versioning

Use **version field in payload** combined with **topic naming convention**:

```t
Current: "disaster.alerts"        # v1 (implicit)
Future:  "disaster.alerts.v2"     # Breaking changes
```

### Version Management Policy

| Scenario | Approach | Example |
|----------|----------|---------|
| **Add optional field** | No version bump; add with default | Add `priority: "HIGH"` with default `"MEDIUM"` |
| **Rename field** | Add old field as deprecated, minor version | v1.1: support both `disaster_type` and `disasterType` |
| **Remove field** | Deprecate first (v1.1), remove in v2.0 | v1.1: deprecate `legacy_field`, v2.0: remove |
| **Change enum values** | Add new enum, map old → new in transformer | Support both `HEATWAVE` and `EXTREME_HEAT` in v1.1 |
| **Change field type** | Breaking change → new topic `disaster.alerts.v2` | timestamp: string → long (milliseconds) |

### Version Header

Include version in Kafka message header for runtime negotiation:

```java
.setHeader("schema_version", "1.0")
```

### Deprecation Timeline

- **v1.0**: Current version (active use)
- **v1.1+**: Minor updates (backward compatible)
- **v2.0**: Major breaking changes → new topic `disaster.alerts.v2`
- **Sunset**: Keep v1 topic alive for 3 months minimum after v2 launch for graceful migration

---

## 7. Consumer Configuration (National EOC)

### Updated KafkaConsumerConfig.java

```java
package distsys26.national_eoc.config;

import distsys26.local_eoc.sensor_reading_processor.models.AlertMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, AlertMessage> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, AlertMessage.class.getName());
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
}
```

### Consumer Listener

```java
package distsys26.national_eoc.kafka;

import distsys26.local_eoc.sensor_reading_processor.models.AlertMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertConsumer {
    
    @KafkaListener(topics = "disaster.alerts", groupId = "national-eoc-group")
    public void consumeAlert(AlertMessage alert) {
        log.info("Received alert: event_id={}, area_id={}, disaster_type={}, alert_level={}", 
            alert.getEventId(), alert.getAreaId(), alert.getDisasterType(), alert.getAlertLevel());
        
        // Process alert
        processAlert(alert);
    }
    
    private void processAlert(AlertMessage alert) {
        // Aggregate, log, escalate, etc.
    }
}
```

---

## 8. Example: End-to-End Flow

### Sending a FLOOD alert from Local EOC (HCM)

**Request Body:**

```json
{
  "area_id": "HCM",
  "disaster_type": "FLOOD",
  "alert_level": "RED",
  "measurements": [
    {"sensor_type": "WATER_LEVEL", "value": 2.8, "unit": "m", "confidence": 0.95},
    {"sensor_type": "RAINFALL", "value": 450, "unit": "mm/h", "confidence": 0.92}
  ]
}
```

**Produced to Kafka:**

- **Topic:** `disaster.alerts`
- **Partition Key:** `HCM`
- **Message ID:** `550e8400-e29b-41d4-a716-446655440000` (auto-generated UUID)
- **Timestamp:** `2026-05-31T14:30:00Z` (auto-generated)

**Consumed by National EOC:**

```t
Received alert: event_id=550e8400-e29b-41d4-a716-446655440000, 
                area_id=HCM, 
                disaster_type=FLOOD, 
                alert_level=RED
```

---

## 9. Testing Checklist

- [ ] Validate schema with sample JSON
- [ ] Test UUID generation and validation
- [ ] Test area_id pattern enforcement (uppercase only)
- [ ] Test timestamp ISO 8601 parsing
- [ ] Test confidence range 0.0–1.0
- [ ] Test non-empty measurements array
- [ ] End-to-end producer → consumer flow
- [ ] Verify message key partitioning by area_id
- [ ] Test with Kafka CLI tools:

  ```bash
  # Consume from topic
  kafka-console-consumer.sh --topic disaster.alerts \
    --from-beginning \
    --bootstrap-server localhost:9092 \
    --property print.key=true \
    --property print.timestamp=true
  ```

---

## 10. Deployment Checklist

- [ ] Create Kafka topic with correct partitions/replication
- [ ] Deploy producer service (Local EOC)
- [ ] Deploy consumer service (National EOC)
- [ ] Validate schema compliance with JSON Schema validator
- [ ] Set up monitoring/alerts for producer failures
- [ ] Document area_id naming convention for operations team
- [ ] Set up alerting when `alert_level == RED`
