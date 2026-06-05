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
    private Collection<SensorNode> pendingNodes = null;

    public MapPanel() {
        setStyle("-fx-background-color: #060e18;");
        VBox.setVgrow(this, Priority.ALWAYS);

        WebView webView = new WebView();
        VBox.setVgrow(webView, Priority.ALWAYS);
        engine = webView.getEngine();

        engine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                pageReady = true;
                System.out.println("Map loaded.");
                if (pendingNodes != null) {
                    plotNodes(pendingNodes);
                    pendingNodes = null;
                }
            }
        });

        getChildren().add(webView);
        loadMapPage();
    }

    public void loadMapPage() {
        Path fsPath = Paths.get("src", "main", "resources", "map.html");
        if (Files.exists(fsPath)) {
            engine.load(fsPath.toAbsolutePath().toUri().toString());
            return;
        }
        URL res = getClass().getResource("/map.html");
        if (res != null) {
            engine.load(res.toExternalForm());
        }
    }

    public void initNodes(Collection<SensorNode> nodes) {
        if (pageReady) plotNodes(nodes);
        else pendingNodes = nodes;
    }

    private void plotNodes(Collection<SensorNode> nodes) {
        for (SensorNode node : nodes) {
            String js = String.format(
                "addSensorNode('%s','%s',%f,%f,'%s','%s');",
                node.getId(), node.getNameVn(),
                node.getLatPx(), node.getLngPx(),
                node.getType().toColorHex(), node.getStatus().name()
            );
            try { engine.executeScript(js); }
            catch (Exception e) { System.err.println("JS error: " + e.getMessage()); }
        }
    }

    public void updateNode(SensorNode node) {
        if (!pageReady) return;
        try {
            engine.executeScript(String.format(
                "updateNodeStatus('%s','%s');", node.getId(), node.getStatus().name()
            ));
        } catch (Exception e) { System.err.println("JS update error: " + e.getMessage()); }
    }
}
