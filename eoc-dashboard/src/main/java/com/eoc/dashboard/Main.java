package com.eoc.dashboard;

import com.eoc.dashboard.model.*;
import com.eoc.dashboard.service.*;
import com.eoc.dashboard.service.LiveAlertPoller;
import com.eoc.dashboard.view.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.*;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            Path csvPath = resolveCsvPath(stage);
            if (csvPath == null) { stage.close(); return; }
            buildUI(stage, csvPath);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog(stage, "Startup failed", e);
        }
    }

    private void buildUI(Stage stage, Path csvPath) throws IOException {

        CsvDataLoader loader = new CsvDataLoader(csvPath);
        loader.load();

        SimulationService sim = new SimulationService();
        sim.loadNodes(loader.getNodes());

        List<String> errors = new ArrayList<>();

        HeaderBar header = safeBuild("HeaderBar", errors, () -> {
            HeaderBar h = new HeaderBar();
            h.setNodeCount(loader.getNodes().size());
            return h;
        });

        ScenarioPanel scenarioPanel = safeBuild("ScenarioPanel", errors, () -> {
            ScenarioPanel s = new ScenarioPanel();
            s.loadScenarios(loader.getScenarios());
            return s;
        });

        KafkaPanel kafkaPanel = safeBuild("KafkaPanel", errors, () -> {
            KafkaPanel k = new KafkaPanel();
            k.loadQueues(loader.getKafkaQueues());
            return k;
        });

        EventLogPanel eventLogPanel = safeBuild("EventLogPanel", errors, () -> {
            EventLogPanel e = new EventLogPanel();
            e.bindLog(sim.getEventLog());
            return e;
        });

        NodeStatusPanel nodeStatusPanel = safeBuild("NodeStatusPanel", errors, () -> {
            NodeStatusPanel n = new NodeStatusPanel();
            n.loadNodes(loader.getNodes());
            return n;
        });

        LiveSensorPanel liveSensorPanel = safeBuild("LiveSensorPanel", errors, LiveSensorPanel::new);

        if (scenarioPanel != null) scenarioPanel.setOnScenarioSelected(sim::runScenario);
        sim.addNodeChangeListener(node -> {
            if (nodeStatusPanel != null) nodeStatusPanel.updateNode(node);
        });

        LiveAlertPoller poller = new LiveAlertPoller(sim);
        if (liveSensorPanel != null) {
            poller.setOnSensorUpdate(liveSensorPanel::updateReadings);
        }
        poller.start();
        stage.setOnCloseRequest(e -> poller.stop());

        // Left column: Scenarios + Node Status + Live Sensor Readings
        VBox leftColumn = new VBox();
        leftColumn.setStyle("-fx-background-color: #060e18;");
        if (scenarioPanel    != null) leftColumn.getChildren().add(scenarioPanel);
        if (nodeStatusPanel  != null) leftColumn.getChildren().add(nodeStatusPanel);
        if (liveSensorPanel  != null) leftColumn.getChildren().add(liveSensorPanel);

        ScrollPane leftScroll = new ScrollPane(leftColumn);
        leftScroll.setFitToWidth(true);
        leftScroll.setFitToHeight(true);
        leftScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        leftScroll.setStyle("-fx-background-color: #060e18; -fx-background: #060e18;");
        leftScroll.setPrefWidth(500);
        leftScroll.setMinWidth(400);

        // Divider
        Region divider = new Region();
        divider.setPrefWidth(1);
        divider.setMinWidth(1);
        divider.setMaxWidth(1);
        divider.setStyle("-fx-background-color: #1a2e42;");

        // Right column: Kafka Queue + Event Log (expands)
        VBox rightColumn = new VBox();
        rightColumn.setStyle("-fx-background-color: #060e18;");
        VBox.setVgrow(rightColumn, Priority.ALWAYS);
        if (kafkaPanel    != null) rightColumn.getChildren().add(kafkaPanel);
        if (eventLogPanel != null) {
            rightColumn.getChildren().add(eventLogPanel);
            VBox.setVgrow(eventLogPanel, Priority.ALWAYS);
        }

        ScrollPane rightScroll = new ScrollPane(rightColumn);
        rightScroll.setFitToWidth(true);
        rightScroll.setFitToHeight(true);
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScroll.setStyle("-fx-background-color: #060e18; -fx-background: #060e18;");
        HBox.setHgrow(rightScroll, Priority.ALWAYS);

        // Main body
        HBox body = new HBox();
        body.setStyle("-fx-background-color: #060e18;");
        VBox.setVgrow(body, Priority.ALWAYS);
        body.getChildren().addAll(leftScroll, divider, rightScroll);

        // Full root
        VBox root = new VBox();
        root.setStyle("-fx-background-color: #060e18;");
        if (header != null) root.getChildren().add(header);
        root.getChildren().add(body);
        VBox.setVgrow(body, Priority.ALWAYS);

        Scene scene = new Scene(root, 1380, 860);
        scene.setFill(Color.web("#060e18"));

        stage.setTitle("Vietnam Disaster EOC — Early Warning System");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
        stage.toFront();

        System.out.println("UI rendered. Errors: " + errors.size());
    }

    // ---- Safe builder ----
    @FunctionalInterface interface Builder<T> { T build() throws Exception; }

    private <T> T safeBuild(String name, List<String> errors, Builder<T> b) {
        try {
            T r = b.build();
            System.out.println("  [OK]   " + name);
            return r;
        } catch (Exception e) {
            String msg = "  [FAIL] " + name + ": " + e.getClass().getSimpleName() + ": " + e.getMessage();
            System.err.println(msg);
            e.printStackTrace();
            errors.add(msg);
            return null;
        }
    }

    // ---- CSV resolution ----
    private Path resolveCsvPath(Stage stage) {
        Path p1 = Paths.get("data", "sensor_data.csv");
        if (Files.exists(p1)) { System.out.println("CSV: " + p1.toAbsolutePath()); return p1; }

        try {
            Path jarDir = Paths.get(
                Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            ).getParent();
            for (int i = 0; i < 3; i++) {
                if (jarDir == null) break;
                Path candidate = jarDir.resolve("data/sensor_data.csv");
                if (Files.exists(candidate)) { System.out.println("CSV: " + candidate.toAbsolutePath()); return candidate; }
                jarDir = jarDir.getParent();
            }
        } catch (URISyntaxException ignored) {}

        Alert info = new Alert(Alert.AlertType.INFORMATION,
            "Could not find data/sensor_data.csv.\nPlease locate it manually.", ButtonType.OK);
        info.setTitle("Locate CSV"); info.setHeaderText("Data file not found");
        info.showAndWait();

        FileChooser fc = new FileChooser();
        fc.setTitle("Open sensor_data.csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        File chosen = fc.showOpenDialog(stage);
        return chosen != null ? chosen.toPath() : null;
    }

    // ---- Error dialog ----
    private void showErrorDialog(Stage stage, String header, Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error"); alert.setHeaderText(header);
        alert.setContentText(e.getMessage());
        TextArea ta = new TextArea(sw.toString());
        ta.setEditable(false);
        ta.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");
        alert.getDialogPane().setExpandableContent(ta);
        alert.getDialogPane().setExpanded(true);
        alert.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}
