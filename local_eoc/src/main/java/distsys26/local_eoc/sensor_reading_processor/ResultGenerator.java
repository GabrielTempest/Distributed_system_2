package distsys26.local_eoc.sensor_reading_processor;

import distsys26.local_eoc.sensor_reading_processor.models.*;
import java.util.List;

import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Generates alert data by processing batches of sensor readings by 
 *      grouping them by sensor type, 
 *      calculating their average values, 
 *      evaluating their scores, 
 *      and stamping them with alert levels.
 */
@Service
public class ResultGenerator {
    private Cache cache;
    private String last_flush_time;
    private List<SensorReading> last_batch;

    public ResultGenerator(Cache cache) {
        this.cache = cache;
    }

    public String getLastFlushTime() {
        return this.last_flush_time;
    }

    /**
     * Flushes the cache to get a batch of sensor readings, 
     *      processes them to generate alert data, 
     *      and updates the last flush time.
     */
    public List<AlertData> generate() {
        last_batch = cache.flush();
        this.last_flush_time = cache.last_flush_time;
        if (last_batch.isEmpty()) {
            System.out.println("No sensor readings to process at " + this.last_flush_time);
            return new ArrayList<>();
        }

        return AlertStamper.stamp(SensorProcessor.processBatch(last_batch));
    }

    public void debugPrint(List<AlertData> alerts) {
        System.out.println("\nGenerated alerts: " + alerts.size());
        for (AlertData alert : alerts) {
            System.out.println("Alert: {" +
                    "\n\tDisaster: " + alert.getDisasterType() +
                    "\n\tLevel: " + alert.getAlertLevel() +
                    "\n\tMeasurements: " + alert.measurementsToString() +
                    "\n}"
            );
        }
    }

    public void debugPrintCache() {
        cache.debugPrint(last_batch);
    }
}
