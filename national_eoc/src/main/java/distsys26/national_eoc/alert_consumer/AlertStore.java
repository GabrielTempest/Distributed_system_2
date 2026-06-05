package distsys26.national_eoc.alert_consumer;

import distsys26.national_eoc.alert_consumer.models.Alert;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Component
public class AlertStore {

    private static final int MAX_SIZE = 200;
    private final Deque<Alert> alerts = new ArrayDeque<>();

    public synchronized void add(Alert alert) {
        if (alerts.size() >= MAX_SIZE) alerts.pollFirst();
        alerts.addLast(alert);
    }

    public synchronized List<Alert> getAll() {
        return new ArrayList<>(alerts);
    }
}
