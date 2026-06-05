package distsys26.local_eoc.sensor_reading_processor;

import distsys26.local_eoc.enums.DisasterType;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final Cache cache;

    public AlertController(Cache cache) {
        this.cache = cache;
    }

    @GetMapping("/latest")
    public Map<String, String> getLatestAlerts() {
        Map<String, String> result = new LinkedHashMap<>();
        for (DisasterType type : DisasterType.values()) {
            result.put(type.name(), cache.getLastPublished(type).name());
        }
        return result;
    }
}
