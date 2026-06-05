package com.eoc.dashboard.service;

import javafx.application.Platform;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Polls Local EOC every 5 seconds for:
 *  - GET /api/alerts/latest   → alert levels per disaster type
 *  - GET /api/readings/latest → latest sensor value per sensor type
 */
public class LiveAlertPoller {

    private static final String ALERTS_URL   = "http://localhost:8081/api/alerts/latest";
    private static final String READINGS_URL = "http://localhost:8081/api/readings/latest";
    private static final int    POLL_INTERVAL_SECONDS = 5;
    private static final Pattern JSON_ENTRY = Pattern.compile("\"(\\w+)\"\\s*:\\s*\"([^\"]+)\"");

    private final SimulationService      sim;
    private final HttpClient             httpClient;
    private final ScheduledExecutorService scheduler;

    private final Map<String, String> lastAlertState = new HashMap<>();

    // Called on the JavaFX thread whenever sensor readings are refreshed
    private Consumer<Map<String, String>> onSensorUpdate;

    public LiveAlertPoller(SimulationService sim) {
        this.sim = sim;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "live-alert-poller");
            t.setDaemon(true);
            return t;
        });
    }

    public void setOnSensorUpdate(Consumer<Map<String, String>> callback) {
        this.onSensorUpdate = callback;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::poll, 0, POLL_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private void poll() {
        pollAlerts();
        pollReadings();
    }

    private void pollAlerts() {
        try {
            String body = get(ALERTS_URL);
            if (body == null) return;
            Map<String, String> alerts = parseJson(body);
            for (Map.Entry<String, String> entry : alerts.entrySet()) {
                String type  = entry.getKey();
                String level = entry.getValue();
                if (!level.equals(lastAlertState.get(type))) {
                    lastAlertState.put(type, level);
                    Platform.runLater(() -> sim.onLiveAlert(type, level));
                }
            }
        } catch (Exception ignored) {}
    }

    private void pollReadings() {
        try {
            String body = get(READINGS_URL);
            if (body == null || onSensorUpdate == null) return;
            Map<String, String> readings = parseJson(body);
            if (!readings.isEmpty()) {
                Platform.runLater(() -> onSensorUpdate.accept(readings));
            }
        } catch (Exception ignored) {}
    }

    private String get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();
        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200 ? response.body() : null;
    }

    /** Parses {"KEY":"value with spaces"} — handles values that contain spaces (e.g. "12.50 m"). */
    private Map<String, String> parseJson(String json) {
        Map<String, String> result = new HashMap<>();
        Matcher m = JSON_ENTRY.matcher(json);
        while (m.find()) {
            result.put(m.group(1), m.group(2));
        }
        return result;
    }
}
