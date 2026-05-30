package distsys26.sensor_simulating_service;

import distsys26.sensor_simulating_service.enums.SensorType;
import distsys26.sensor_simulating_service.models.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

/**
 * Service class responsible for simulating sensor readings and sending them to the local EOC.
 * Uses Spring's @Scheduled annotation to periodically generate and send sensor readings for each sensor type.
 * Schedules rates should be a factor of 10s (flush rate to processor of local EOC) 
 *      to ensure number of readings per sensor type per flush is consistent.
 */
@Service
public class SensorSimulatorService {
    private final RestTemplate restTemplate = new RestTemplate();
    private String EOC_URL;

    public SensorSimulatorService(@Value("${api.base-url}") String baseUrl) {
        this.EOC_URL = baseUrl + "/api/readings";
    }

    private ConcurrentHashMap<SensorType, Double> sensorTypeToSeedingValueMap = 
                    SensorReadingGenerator.sensorReadingSeedGenerator();
    private ConcurrentHashMap<SensorType, Integer> sensorTypeToSensorCountMap = 
                    SensorReadingGenerator.generatedSensorCount();

    @Scheduled(fixedRate = 2500)
    public void sendBarometer() {
        postReading(SensorType.BAROMETER);
    }

    @Scheduled(fixedRate = 2500)
    public void sendAnemometer() {
        postReading(SensorType.ANEMOMETER);
    }

    @Scheduled(fixedRate = 1000)
    public void postRainGauge() {
        postReading(SensorType.RAINGAUGE);
    }
    
    @Scheduled(fixedRate = 5000)
    public void sendFloodGauge() {
        postReading(SensorType.FLOODGAUGE);
    }

    @Scheduled(fixedRate = 5000)
    public void sendVibration() {
        postReading(SensorType.VIBRATION);
    }

    @Scheduled(fixedRate = 2500)
    public void sendTilt() {
        postReading(SensorType.TILT);
    }

    @Scheduled(fixedRate = 2500)
    public void sendSoilMoisture() {
        postReading(SensorType.SOILMOISTURE);
    }








    /**
     * Post sensor readings for a specific sensor type to the local EOC and update the seeding values.
     * @param sensorType the type of sensor for which to post readings
     */
    private void postReading(SensorType sensorType) {
        Integer sensorCount = sensorTypeToSensorCountMap.get(sensorType);
        Double sharedSeedingValue = sensorTypeToSeedingValueMap.get(sensorType);
        if (sensorCount == null || sharedSeedingValue == null) {
            System.out.println("Sensor type not found in the maps: " + sensorType);
            return;
        }
        for (int i = 0; i < sensorCount; i++) {
            SensorReading reading = SensorReadingGenerator.generateRandomSensorReading(sensorType, i, sharedSeedingValue);
            post(reading);
        }
        SensorValue sensorValue = Mapping.sensorToGeneralValuesMap.get(sensorType);
        System.out.printf("Posted %d readings for sensor type: %s, seeding value: %f (%s), range: [%.2f, %.2f]\n", 
                                sensorCount, sensorType, sharedSeedingValue, 
                                sensorValue.unit, sensorValue.general_min, sensorValue.general_max
                            );
        SensorReadingGenerator.updateSeedingValues(sensorType, sensorTypeToSeedingValueMap);
    }

    /**
     * Send a sensor reading to the local EOC via a http POST request.
     * @param reading the sensor reading to send
     */
    private void post(SensorReading reading) {
        try {
            restTemplate.postForObject(EOC_URL, reading, String.class);
        } catch (ResourceAccessException e) {
            System.out.println("Failed to connect to the local EOC at " + EOC_URL + 
                                ". Please ensure the local EOC is running and the URL is correct."
                                );
        } catch (Exception e) {
            System.out.println("An unexpected error occurred while posting sensor reading." + 
                                    " Error message: " + e.getMessage()
                                );
        }
    }

}