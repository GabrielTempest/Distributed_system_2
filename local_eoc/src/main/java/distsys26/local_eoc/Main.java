package distsys26.local_eoc;

import distsys26.local_eoc.sensor_reading_processor.ResultGenerator;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Run flow at the local EOC.
 */
@Service
public class Main {
    ResultGenerator resultGenerator = new ResultGenerator();

    /**
     * Flush sensor readings from temporary cache and generate alert messages every 5 seconds.
     */
    @Scheduled(fixedRate = 5000)
    public void run() {
        resultGenerator.generate();
    }
}
