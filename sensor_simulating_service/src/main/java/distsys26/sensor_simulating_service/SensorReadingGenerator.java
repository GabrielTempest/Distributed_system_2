package distsys26.sensor_simulating_service;

import distsys26.sensor_simulating_service.enums.SensorType;
import distsys26.sensor_simulating_service.models.*;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

public final class SensorReadingGenerator {
    private static final Random random = new Random();

    public static final ConcurrentHashMap<SensorType, Integer> generatedSensorCount() {
        ConcurrentHashMap<SensorType, Integer> sensorCountMap = new ConcurrentHashMap<>();
        for (SensorType sensorType : SensorType.values()) {
            sensorCountMap.put(sensorType, randomNoOfSensors());
        }
        return sensorCountMap;
    }

    /**
     * Create a shared sensor reading values for a local EOC
     * @return a dictionary of sensor type to share reading
     */
    public static final ConcurrentHashMap<SensorType, Double> sensorReadingSeedGenerator() {
        ConcurrentHashMap<SensorType, Double> localAreaReading = new ConcurrentHashMap<>();
        for (SensorType sensorType : SensorType.values()) {
            SensorValue sensorValue = Mapping.sensorToGeneralValuesMap.get(sensorType);
            double min = sensorValue.general_min;
            double max = sensorValue.general_max;
            double value = Math.random() * (max - min) + min;
            localAreaReading.put(sensorType, value);
        }
        return localAreaReading;
    }

    /**
     * Update the seeding value to fluctuate 0-3% from the original value to simulate changes in the environment
     * @param sensorType the type of sensor for which to update the seeding value
     * @param sensorTypeToSeedingValueMap the map of sensor type to seeding value to update
     */
    public static final void updateSeedingValues(
                                    SensorType sensorType,
                                    ConcurrentHashMap<SensorType, Double> sensorTypeToSeedingValueMap
    ) {
        double currentSeedingValue = sensorTypeToSeedingValueMap.get(sensorType);
        double fluctuation = random.nextDouble() * 0.06 - 0.03; // -3% to +3%
        sensorTypeToSeedingValueMap.put(sensorType, currentSeedingValue * (1 + fluctuation));
    }

    /**
     * Generates a fake sensor reading with random value based on the sensor type.
     * @param sensorType the type of sensor for which to generate the reading
     * @param index the index of the sensor of the given type to generate the reading for (used to create unique sensor IDs)
     * @param shared_seeding_value a shared seeding value to ensure that readings from different sensors are correlated
     * @return a SensorReading object with the generated value
     */
    public static final SensorReading generateRandomSensorReading(SensorType sensorType, int index, double shared_seeding_value) {
        SensorValue sensorValue = Mapping.sensorToGeneralValuesMap.get(sensorType);
        return new SensorReading(
            sensorValue.sensorIdPrefix + "-" + String.format("%08d", index),
            sensorType,
            generateSeedValue(sensorValue, shared_seeding_value),
            sensorValue.unit,
            Instant.now().toString()
        );
    }

    /**
     * Generate a random value different from seed at most by 1% of the range
     * @param sensorValue - the general values for the sensor type
     * @param shared_seeding_value 
     * @return
     */
    private static final double generateSeedValue(SensorValue sensorValue, double shared_seeding_value) {
        double random_offset = random.nextDouble() * 0.02 - 0.01;
        double simulated_value = shared_seeding_value * (1 + random_offset);
        if (simulated_value < sensorValue.general_min) {
            simulated_value = sensorValue.general_min;
        } else if (simulated_value > sensorValue.general_max) {
            simulated_value = sensorValue.general_max;
        }
        return simulated_value;
    }

    /**
     * Generate a random number of sensors
     * @return the number of sensors to generate (20-40)
     */
    private static final int randomNoOfSensors() {
        int min = 20;
        int max = 40;
        return min + random.nextInt(max - min + 1);
    }
}

