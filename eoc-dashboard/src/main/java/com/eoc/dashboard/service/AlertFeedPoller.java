package com.eoc.dashboard.service;

import com.eoc.dashboard.view.AlertTablePanel.AlertRow;
import javafx.application.Platform;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.regex.*;

/**
 * Polls GET http://localhost:8090/api/ui/alerts every 3 seconds.
 * Parses the JSON array returned by national_eoc's UIAlertController
 * and delivers a List<AlertRow> to the UI on the JavaFX thread.
 */
public class AlertFeedPoller {

    private static final String URL = "http://localhost:8090/api/ui/alerts";
    private static final int    POLL_SECONDS = 3;

    // Matches every JSON object in the array
    private static final Pattern OBJECT  = Pattern.compile("\\{[^{}]*\\}");
    // Matches "key":"value" (value may contain spaces)
    private static final Pattern KV      = Pattern.compile("\"(\\w+)\"\\s*:\\s*\"([^\"]*)\"");

    private final HttpClient             http;
    private final ScheduledExecutorService scheduler;
    private final Consumer<List<AlertRow>> onUpdate;

    public AlertFeedPoller(Consumer<List<AlertRow>> onUpdate) {
        this.onUpdate  = onUpdate;
        this.http      = HttpClient.newBuilder()
                            .connectTimeout(Duration.ofSeconds(3))
                            .build();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "alert-feed-poller");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::poll, 0, POLL_SECONDS, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private void poll() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return;

            List<AlertRow> rows = parse(resp.body());
            Platform.runLater(() -> onUpdate.accept(rows));

        } catch (Exception ignored) {
            // national_eoc offline — silently skip
        }
    }

    private List<AlertRow> parse(String json) {
        List<AlertRow> result = new ArrayList<>();
        Matcher obj = OBJECT.matcher(json);
        while (obj.find()) {
            String block = obj.group();
            var fields   = new java.util.HashMap<String, String>();
            Matcher kv   = KV.matcher(block);
            while (kv.find()) fields.put(kv.group(1), kv.group(2));

            result.add(new AlertRow(
                fields.getOrDefault("alertId",      ""),
                fields.getOrDefault("localEocId",   ""),
                fields.getOrDefault("disasterType", ""),
                fields.getOrDefault("alertType",    ""),
                fields.getOrDefault("message",      ""),
                fields.getOrDefault("timestamp",    "")
            ));
        }
        return result;
    }
}
