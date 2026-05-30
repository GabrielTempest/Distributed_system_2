package distsys26.local_eoc.sensor_reading_processor;

import distsys26.local_eoc.sensor_reading_processor.models.*;
import distsys26.local_eoc.enums.SensorType;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Utility class for 
 *      processing sensor readings and 
 *      evaluating their scores based on predefined thresholds 
 *      for each sensor type.
 */
public final class SensorProcessor {
    private SensorProcessor() {}

    /**
     * Process a batch of sensor readings by calculating the average value for each sensor type and evaluating their scores.
     * @param batch A list of sensor readings to process.
     * @return A HashMap mapping each SensorType to its corresponding SensorScore based on the average readings.
     */
    public static HashMap<SensorType, SensorScore> processBatch(List<SensorReading> batch) {
        List<SensorReading> averageReadings = calculateAverage(batch);
        return evaluateScore(averageReadings);
    }

    /**
     * Calculate the average value for each sensor type in the batch of sensor readings.
     * @param batch A list of sensor readings to process.
     * @return A list of SensorReading objects representing the average value for each sensor type.
     */
    private static List<SensorReading> calculateAverage(List<SensorReading> batch) {
        List<SensorReading> averageReadings = new ArrayList<>();
        Map<SensorType, List<SensorReading>> grouped = batch.stream()
                .collect(Collectors.groupingBy(SensorReading::getSensorType));
        for (List<SensorReading> readings : grouped.values()) {
            double average = readings.stream()
                    .mapToDouble(SensorReading::getValue)
                    .average()
                    .orElse(0.0);
            SensorReading avgReading = SensorReading.builder()
                    .sensorType(readings.get(0).getSensorType())
                    .value(average)
                    .unit(readings.get(0).getUnit())
                    .build();
            averageReadings.add(avgReading);
        }
        return averageReadings;
    }

    /**
     * Evaluate the scores for a list of average sensor readings based on predefined thresholds for each sensor type.
     * @param averageReadings A list of average sensor readings to evaluate.
     * @return A HashMap mapping each SensorType to its corresponding SensorScore.
     */
    private static HashMap<SensorType, SensorScore> evaluateScore(List<SensorReading> averageReadings) {
        HashMap<SensorType, SensorScore> scores = new HashMap<>();
        for (SensorReading reading : averageReadings) {
            int score = evaluateScore(reading);
            scores.put(reading.getSensorType(), new SensorScore(reading.getSensorType(), reading.getValue(), reading.getUnit(), score));
        }
        return scores;
    }

    /**
     * Evaluate the score of a sensor reading based on predefined thresholds for each sensor type.
     * @param reading The sensor reading to evaluate.
     * @return An integer score from 0 to 3, where higher scores indicate higher severity.
     */
    private static int evaluateScore(SensorReading reading) {
        double value = reading.getValue();
        int[] thresholds = Mapping.sensorReadingThresholds.get(reading.getSensorType());
        // Handle case where thresholds are in reverse (decreasing) order (e.g., for air pressure)
        if (thresholds[2] > thresholds[0]) {
            value *= -1;
            for (int i = 0; i < thresholds.length; i++) {
                thresholds[i] *= -1;
            }
        }
        // Determine score based on thresholds
        int score;
        if(value < thresholds[0]) {
            score = 0;
        } else if (value < thresholds[1]) {
            score = 1;
        } else if (value < thresholds[2]) {
            score = 2;
        } else {
            score = 3;
        }
        return score;
    }
}
