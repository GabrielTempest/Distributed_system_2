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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Polls GET http://localhost:8081/api/alerts/latest every 5 seconds and
 * forwards changed alert levels to SimulationService on the JavaFX thread.
 */
public class LiveAlertPoller {

    private static final String ALERTS_URL = "http://localhost:8081/api/alerts/latest";
    private static final int POLL_INTERVAL_SECONDS = 5;
    private static final Pattern JSON_ENTRY = Pattern.compile("\"(\\w+)\"\\s*:\\s*\"(\\w+)\"");

    private final SimulationService sim;
    private final HttpClient httpClient;
    private final ScheduledExecutorService scheduler;
    private final Map<String, String> lastState = new HashMap<>();

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

    public void start() {
        scheduler.scheduleAtFixedRate(this::poll, 0, POLL_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private void poll() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ALERTS_URL))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, String> alerts = parseJson(response.body());
                for (Map.Entry<String, String> entry : alerts.entrySet()) {
                    String type  = entry.getKey();
                    String level = entry.getValue();
                    if (!level.equals(lastState.get(type))) {
                        lastState.put(type, level);
                        Platform.runLater(() -> sim.onLiveAlert(type, level));
                    }
                }
            }
        } catch (Exception ignored) {
            // Backend offline — silently skip until it comes back
        }
    }

    /** Parses {"FLOOD":"GREEN","TYPHOON":"YELLOW"} without a JSON library. */
    private Map<String, String> parseJson(String json) {
        Map<String, String> result = new HashMap<>();
        Matcher m = JSON_ENTRY.matcher(json);
        while (m.find()) {
            result.put(m.group(1), m.group(2));
        }
        return result;
    }
}
