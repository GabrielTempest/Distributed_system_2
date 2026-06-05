package distsys26.national_eoc.alert_consumer;

import distsys26.national_eoc.alert_consumer.models.Alert;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ui")
public class UIAlertController {

    private final AlertStore alertStore;

    public UIAlertController(AlertStore alertStore) {
        this.alertStore = alertStore;
    }

    @GetMapping("/alerts")
    public List<Alert> getAlerts() {
        return alertStore.getAll();
    }
}
