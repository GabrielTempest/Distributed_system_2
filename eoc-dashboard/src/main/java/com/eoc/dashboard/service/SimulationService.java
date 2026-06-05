package com.eoc.dashboard.service;

import com.eoc.dashboard.model.*;
import com.eoc.dashboard.model.SensorNode.NodeStatus;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

// Maps disaster types (from Local EOC) to the node IDs most affected by each
// FLOOD → Can Tho (Mekong Delta), TYPHOON → Da Nang (central coast), LANDSLIDE → Nghe An (mountains)

/**
 * Drives all live simulation:
 *  • Scenario event-log playback (one log line every ~600 ms)
 *  • Periodic battery / signal jitter on all nodes
 *  • Node status changes during scenarios
 */
public class SimulationService {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final Map<String, String> DISASTER_NODE_MAP = Map.of(
        "FLOOD",     "CT",
        "TYPHOON",   "DN",
        "LANDSLIDE", "NA"
    );

    // Observable list that the EventLogPanel binds to
    private final ObservableList<String> eventLog =
            FXCollections.observableArrayList();

    // All loaded nodes, keyed by ID
    private final Map<String, SensorNode> nodeMap = new LinkedHashMap<>();

    // Callbacks registered by the MapPanel to be notified when a node changes
    private final List<Consumer<SensorNode>> nodeChangeListeners = new ArrayList<>();

    // Active animations (cancelled before starting a new scenario)
    private final List<Timeline> activeTimelines = new ArrayList<>();

    private Scenario activeScenario = null;

    // ------------------------------------------------------------------ //
    //  Setup
    // ------------------------------------------------------------------ //

    public void loadNodes(List<SensorNode> nodes) {
        nodeMap.clear();
        for (SensorNode n : nodes) nodeMap.put(n.getId(), n);
        startJitterLoop();
    }

    public ObservableList<String> getEventLog() { return eventLog; }

    public Map<String, SensorNode> getNodeMap() {
        return Collections.unmodifiableMap(nodeMap);
    }

    public void addNodeChangeListener(Consumer<SensorNode> l) {
        nodeChangeListeners.add(l);
    }

    private void notifyNodeChange(SensorNode node) {
        for (Consumer<SensorNode> l : nodeChangeListeners) l.accept(node);
    }

    // ------------------------------------------------------------------ //
    //  Scenario playback
    // ------------------------------------------------------------------ //

    public void runScenario(Scenario scenario) {
        // Cancel any running scenario
        stopAll();
        eventLog.clear();
        activeScenario = scenario;

        SensorNode target = nodeMap.get(scenario.getTargetNodeId());

        List<String> templates = scenario.getLogTemplates();
        if (templates == null || templates.isEmpty()) {
            eventLog.add(now() + " INFO  | Scenario \"" + scenario.getName() + "\" started");
            return;
        }

        AtomicInteger idx = new AtomicInteger(0);

        Timeline tl = new Timeline();
        tl.setCycleCount(templates.size());

        KeyFrame kf = new KeyFrame(Duration.millis(650), e -> {
            int i = idx.getAndIncrement();
            if (i >= templates.size()) return;

            String line = templates.get(i)
                    .replace("{time}", now())
                    .replace("{node}", scenario.getTargetNodeId());

            Platform.runLater(() -> eventLog.add(0, line));

            // Update node status based on scenario severity
            if (target != null) {
                NodeStatus ns = switch (scenario.getSeverity()) {
                    case CRITICAL -> NodeStatus.ERROR;
                    case HIGH     -> NodeStatus.WARNING;
                    default       -> NodeStatus.WARNING;
                };
                Platform.runLater(() -> {
                    target.setStatus(ns);
                    notifyNodeChange(target);
                });
            }
        });

        tl.getKeyFrames().add(kf);
        tl.setOnFinished(e -> {
            if (target != null) {
                Platform.runLater(() -> {
                    target.setStatus(NodeStatus.OK);
                    notifyNodeChange(target);
                });
            }
        });

        activeTimelines.add(tl);
        tl.play();
    }

    // ------------------------------------------------------------------ //
    //  Background jitter loop (simulates live sensor noise)
    // ------------------------------------------------------------------ //

    private void startJitterLoop() {
        Random rng = new Random();

        Timeline jitter = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            // Pick a random node to jitter
            List<SensorNode> all = new ArrayList<>(nodeMap.values());
            if (all.isEmpty()) return;
            SensorNode n = all.get(rng.nextInt(all.size()));

            // Only jitter if no active critical scenario on this node
            if (activeScenario != null
                    && activeScenario.getTargetNodeId().equals(n.getId())
                    && n.getStatus() == NodeStatus.ERROR) return;

            int batt   = Math.max(10, Math.min(100, n.getBattery() + rng.nextInt(5) - 2));
            int signal = Math.max(20, Math.min(100, n.getSignal()  + rng.nextInt(7) - 3));
            n.setBattery(batt);
            n.setSignal(signal);
            n.setLastSeen(now());
            notifyNodeChange(n);
        }));
        jitter.setCycleCount(Timeline.INDEFINITE);
        jitter.play();
        activeTimelines.add(jitter);
    }

    // ------------------------------------------------------------------ //
    //  Live alert integration (REST polling from Local EOC)
    // ------------------------------------------------------------------ //

    /** Called on the JavaFX thread whenever a disaster alert level changes. */
    public void onLiveAlert(String disasterType, String alertLevel) {
        String time = now();
        eventLog.add(0, String.format("[%s] LIVE  | %s alert → %s", time, disasterType, alertLevel));

        String nodeId = DISASTER_NODE_MAP.get(disasterType);
        if (nodeId == null) return;

        SensorNode node = nodeMap.get(nodeId);
        if (node == null) return;

        NodeStatus status = switch (alertLevel) {
            case "RED", "ORANGE" -> NodeStatus.ERROR;
            case "YELLOW"        -> NodeStatus.WARNING;
            default              -> NodeStatus.OK;
        };
        node.setStatus(status);
        node.setLastSeen(time);
        notifyNodeChange(node);
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private void stopAll() {
        activeTimelines.forEach(Timeline::stop);
        activeTimelines.clear();
        startJitterLoop();          // restart jitter
        activeScenario = null;
    }

    private String now() {
        return LocalTime.now().format(TIME_FMT);
    }
}
