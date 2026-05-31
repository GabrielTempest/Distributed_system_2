package com.eoc.dashboard.view;

import com.eoc.dashboard.model.SensorNode;
import javafx.concurrent.Worker;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.nio.file.*;
import java.util.Collection;

public class MapPanel extends VBox {

    private WebEngine engine;
    private boolean   pageReady = false;

    // Nodes waiting to be plotted once page loads
    private Collection<SensorNode> pendingNodes = null;

    public MapPanel() {
        setPrefWidth(420);
        setMinWidth(300);
        setStyle("-fx-background-color: #060e18;");
        VBox.setVgrow(this, Priority.ALWAYS);

        WebView webView = new WebView();
        webView.setStyle("-fx-background-color: #060e18;");
        webView.setPageFill(javafx.scene.paint.Color.web("#060e18"));
        VBox.setVgrow(webView, Priority.ALWAYS);
        engine = webView.getEngine();

        // Allow local file access for the GeoJSON fetch()
        engine.setUserDataDirectory(
            Paths.get(System.getProperty("user.home"), ".eoc-webview").toFile()
        );

        // Listen for page load
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                pageReady = true;
                System.out.println("Map page loaded.");
                if (pendingNodes != null) {
                    plotNodes(pendingNodes);
                    pendingNodes = null;
                }
            } else if (newState == Worker.State.FAILED) {
                System.err.println("Map page FAILED to load: " + engine.getLocation());
            }
        });

        // Log JS errors
        engine.setOnError(e -> System.err.println("WebEngine error: " + e.getMessage()));

        // Load the HTML — try classpath resource first, then filesystem
        loadMapPage();

        getChildren().add(webView);
    }

    private void loadMapPage() {
        // Try next to the sensor_data.csv (works when run from project root)
        Path fsPath = Paths.get("src", "main", "resources", "map.html");
        if (Files.exists(fsPath)) {
            String url = fsPath.toAbsolutePath().toUri().toString();
            System.out.println("Loading map from filesystem: " + url);
            engine.load(url);
            return;
        }

        // Fallback: classpath resource (works from JAR)
        URL res = getClass().getResource("/map.html");
        if (res != null) {
            System.out.println("Loading map from classpath: " + res);
            engine.load(res.toExternalForm());
            return;
        }

        System.err.println("map.html not found! Tried: " + fsPath.toAbsolutePath());
        engine.loadContent(
            "<html><body style='background:#060e18;color:#e06c75;" +
            "font-family:monospace;padding:20px'>" +
            "<b>map.html not found.</b><br>Expected at:<br>" +
            fsPath.toAbsolutePath() + "</body></html>"
        );
    }

    public void initNodes(Collection<SensorNode> nodes) {
        if (pageReady) {
            plotNodes(nodes);
        } else {
            pendingNodes = nodes; // will be plotted once page loads
        }
    }

    private void plotNodes(Collection<SensorNode> nodes) {
        for (SensorNode node : nodes) {
            String js = String.format(
                "addSensorNode('%s', '%s', %f, %f, '%s', '%s');",
                node.getId(),
                node.getNameVn(),
                node.getLatPx(),   // real latitude  (from CSV col 5)
                node.getLngPx(),   // real longitude (from CSV col 6)
                node.getType().toColorHex(),
                node.getStatus().name()
            );
            try {
                engine.executeScript(js);
            } catch (Exception e) {
                System.err.println("JS error for node " + node.getId() + ": " + e.getMessage());
            }
        }
    }

    public void updateNode(SensorNode node) {
        if (!pageReady) return;
        String js = String.format(
            "updateNodeStatus('%s', '%s');",
            node.getId(),
            node.getStatus().name()
        );
        try {
            engine.executeScript(js);
        } catch (Exception e) {
            System.err.println("JS update error: " + e.getMessage());
        }
    }
}
