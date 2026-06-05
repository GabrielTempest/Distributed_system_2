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

public class SimulationService {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ObservableList<String> eventLog =
            FXCollections.observableArrayList();

    private final Map<String, SensorNode> nodeMap = new LinkedHashMap<>();

    // Fires on every change (battery/signal jitter + scenario) → NodeStatusPanel
    private final List<Consumer<SensorNode>> nodeChangeListeners = new ArrayList<>();

    // Fires ONLY on scenario status changes → MapPanel
    private final List<Consumer<SensorNode>> scenarioChangeListeners = new ArrayList<>();

    private final List<Timeline> activeTimelines = new ArrayList<>();
    private Timeline jitterTimeline = null;

    private Scenario activeScenario = null;

    public void loadNodes(List<SensorNode> nodes) {
        nodeMap.clear();
        for (SensorNode n : nodes) nodeMap.put(n.getId(), n);
        startJitterLoop();
    }

    public ObservableList<String> getEventLog() { return eventLog; }
    public Map<String, SensorNode> getNodeMap()  { return Collections.unmodifiableMap(nodeMap); }

    /** Called by NodeStatusPanel — fires on jitter + scenario changes */
    public void addNodeChangeListener(Consumer<SensorNode> l) {
        nodeChangeListeners.add(l);
    }

    /** Called by MapPanel — fires ONLY on scenario status changes, not jitter */
    public void addScenarioChangeListener(Consumer<SensorNode> l) {
        scenarioChangeListeners.add(l);
    }

    private void notifyNodeChange(SensorNode node) {
        for (Consumer<SensorNode> l : nodeChangeListeners) l.accept(node);
    }

    private void notifyScenarioChange(SensorNode node) {
        for (Consumer<SensorNode> l : scenarioChangeListeners) l.accept(node);
    }

    // ------------------------------------------------------------------ //
    //  Scenario playback
    // ------------------------------------------------------------------ //
    public void runScenario(Scenario scenario) {
        stopScenario();
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

            // Update node status — notify BOTH listeners (status panel + map)
            if (target != null) {
                NodeStatus ns = switch (scenario.getSeverity()) {
                    case CRITICAL -> NodeStatus.ERROR;
                    case HIGH     -> NodeStatus.WARNING;
                    default       -> NodeStatus.WARNING;
                };
                Platform.runLater(() -> {
                    target.setStatus(ns);
                    // notify status panel
                    notifyNodeChange(target);
                    notifyScenarioChange(target);  // only map gets this
                });
            }
        });

        tl.getKeyFrames().add(kf);
        tl.setOnFinished(e -> {
            if (target != null) {
                Platform.runLater(() -> {
                    target.setStatus(NodeStatus.OK);
                    notifyNodeChange(target);
                    notifyScenarioChange(target);  // reset map dot color too
                });
            }
            activeScenario = null;
        });

        activeTimelines.add(tl);
        tl.play();
    }

    // ------------------------------------------------------------------ //
    //  Jitter loop — only notifies nodeChangeListeners (NOT map)
    // ------------------------------------------------------------------ //
    private void startJitterLoop() {
        if (jitterTimeline != null) jitterTimeline.stop();
        Random rng = new Random();

        jitterTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            List<SensorNode> all = new ArrayList<>(nodeMap.values());
            if (all.isEmpty()) return;
            SensorNode n = all.get(rng.nextInt(all.size()));

            // Skip if scenario is actively changing this node
            if (activeScenario != null
                    && activeScenario.getTargetNodeId().equals(n.getId())
                    && n.getStatus() == NodeStatus.ERROR) return;

            int batt   = Math.max(10, Math.min(100, n.getBattery() + rng.nextInt(5) - 2));
            int signal = Math.max(20, Math.min(100, n.getSignal()  + rng.nextInt(7) - 3));
            n.setBattery(batt);
            n.setSignal(signal);
            n.setLastSeen(now());

            // Only notify status panel — NOT the map (avoids WebView repaint flash)
            Platform.runLater(() -> notifyNodeChange(n));
        }));

        jitterTimeline.setCycleCount(Timeline.INDEFINITE);
        jitterTimeline.play();
    }

    private void stopScenario() {
        activeTimelines.forEach(Timeline::stop);
        activeTimelines.clear();
        activeScenario = null;
    }

    private String now() {
        return LocalTime.now().format(TIME_FMT);
    }
}
