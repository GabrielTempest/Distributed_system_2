package distsys26.local_eoc.sensor_reading_processor;

import distsys26.local_eoc.alert_publisher.AlertPublisher;
import distsys26.local_eoc.alert_publisher.models.Alert;
import distsys26.local_eoc.enums.AlertLevel;
import distsys26.local_eoc.enums.DisasterType;
import distsys26.local_eoc.enums.SensorType;
import distsys26.local_eoc.sensor_reading_processor.models.SensorReading;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SensorProcessor {

    // [min, max, direction]: direction=1 → high bad, direction=-1 → low bad, direction=2 → |value|/half-range
    private static final Map<SensorType, double[]> RANGES = new EnumMap<>(SensorType.class);
    static {
        RANGES.put(SensorType.FLOODGAUGE,   new double[]{0,   20,   1});
        RANGES.put(SensorType.RAINGAUGE,    new double[]{0,   500,  1});
        RANGES.put(SensorType.ANEMOMETER,   new double[]{0,   60,   1});
        RANGES.put(SensorType.BAROMETER,    new double[]{300, 1100, -1}); // low pressure = danger
        RANGES.put(SensorType.SOILMOISTURE, new double[]{0,   100,  1});
        RANGES.put(SensorType.TILT,         new double[]{-90, 90,   2}); // absolute tilt
        RANGES.put(SensorType.VIBRATION,    new double[]{0,   16,   1});
    }

    private final AlertPublisher alertPublisher;
    private final Cache cache;
    private final String localEocId;

    public SensorProcessor(AlertPublisher alertPublisher, Cache cache,
                           @Value("${eoc.local.id}") String localEocId) {
        this.alertPublisher = alertPublisher;
        this.cache = cache;
        this.localEocId = localEocId;
    }

    public void process(List<SensorReading> batch) {
        Map<SensorType, Double> averages = batch.stream()
                .collect(Collectors.groupingBy(
                        SensorReading::getSensorType,
                        Collectors.averagingDouble(SensorReading::getValue)
                ));

        for (DisasterType disaster : DisasterType.values()) {
            AlertLevel current = classify(disaster, averages);
            AlertLevel last = cache.getLastPublished(disaster);

            if (current == last) continue;

            cache.setLastPublished(disaster, current);
            String msg = current == AlertLevel.GREEN
                    ? disaster + " all-clear: levels returned to normal."
                    : buildMessage(disaster, current, averages);
            alertPublisher.publish(new Alert(
                    UUID.randomUUID().toString(),
                    localEocId,
                    disaster,
                    current,
                    msg,
                    Instant.now().toString()
            ));
        }
    }

    private AlertLevel classify(DisasterType disaster, Map<SensorType, Double> averages) {
        double maxSeverity = 0.0;
        for (SensorType sensor : Mapping.disasterToSensorMap.get(disaster)) {
            Double avg = averages.get(sensor);
            if (avg == null) continue;
            maxSeverity = Math.max(maxSeverity, normalize(sensor, avg));
        }
        return toAlertLevel(maxSeverity);
    }

    private double normalize(SensorType sensor, double value) {
        double[] r = RANGES.get(sensor);
        double min = r[0], max = r[1], dir = r[2];
        double range = max - min;
        if (dir == 1)  return Math.min(1.0, Math.max(0.0, (value - min) / range));
        if (dir == -1) return Math.min(1.0, Math.max(0.0, (max - value) / range));
        // dir == 2: absolute-value (TILT: centre is 0, half-range = 90)
        return Math.min(1.0, Math.abs(value) / (range / 2));
    }

    private AlertLevel toAlertLevel(double severity) {
        if (severity < 0.4) return AlertLevel.GREEN;
        if (severity < 0.6) return AlertLevel.YELLOW;
        if (severity < 0.8) return AlertLevel.ORANGE;
        return AlertLevel.RED;
    }

    private String buildMessage(DisasterType disaster, AlertLevel level,
                                Map<SensorType, Double> averages) {
        StringBuilder sb = new StringBuilder()
                .append(disaster).append(" alert ").append(level).append(". Readings: ");
        for (SensorType st : Mapping.disasterToSensorMap.get(disaster)) {
            Double v = averages.get(st);
            if (v != null) sb.append(st).append("=").append(String.format("%.2f", v)).append(" ");
        }
        return sb.toString().trim();
    }
}
