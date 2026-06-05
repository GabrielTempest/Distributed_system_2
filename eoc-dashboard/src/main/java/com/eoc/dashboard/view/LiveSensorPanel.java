package com.eoc.dashboard.view;

import javafx.geometry.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Displays real-time sensor readings received from GET /api/readings/latest.
 * Each row shows: sensor name | current value | unit
 */
public class LiveSensorPanel extends VBox {

    private static final Map<String, String> FRIENDLY_NAMES = new LinkedHashMap<>();
    static {
        FRIENDLY_NAMES.put("FLOODGAUGE",   "Flood Gauge");
        FRIENDLY_NAMES.put("RAINGAUGE",    "Rain Gauge");
        FRIENDLY_NAMES.put("ANEMOMETER",   "Anemometer");
        FRIENDLY_NAMES.put("BAROMETER",    "Barometer");
        FRIENDLY_NAMES.put("SOILMOISTURE", "Soil Moisture");
        FRIENDLY_NAMES.put("TILT",         "Tilt Sensor");
        FRIENDLY_NAMES.put("VIBRATION",    "Vibration");
    }

    // Thresholds: [warn, critical] — mirrors SensorProcessor in Local EOC
    private static final Map<String, double[]> WARN_CRIT = new LinkedHashMap<>();
    static {
        WARN_CRIT.put("FLOODGAUGE",   new double[]{8.0,  16.0});  // metres
        WARN_CRIT.put("RAINGAUGE",    new double[]{200.0, 400.0}); // mm/h
        WARN_CRIT.put("ANEMOMETER",   new double[]{24.0,  48.0}); // km/h
        WARN_CRIT.put("BAROMETER",    new double[]{760.0, 620.0}); // hPa — low = danger (inverted)
        WARN_CRIT.put("SOILMOISTURE", new double[]{40.0,  60.0}); // %
        WARN_CRIT.put("TILT",         new double[]{36.0,  54.0}); // degrees (absolute)
        WARN_CRIT.put("VIBRATION",    new double[]{6.4,   9.6});  // g
    }

    private final Map<String, Label> valueLabels = new LinkedHashMap<>();
    private final Label waitingLabel;

    public LiveSensorPanel() {
        setSpacing(0);
        setPadding(new Insets(20, 20, 16, 20));
        setStyle("-fx-background-color: #0a1628;");
        setBorder(new Border(new BorderStroke(
            Color.web("#1a2e42"), BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY, new BorderWidths(0, 0, 1, 0)
        )));

        getChildren().add(sectionHeader("LIVE SENSOR READINGS"));

        waitingLabel = new Label("Waiting for backend data…");
        waitingLabel.setFont(Font.font("Monospace", 11));
        waitingLabel.setTextFill(Color.web("#2a4050"));
        waitingLabel.setPadding(new Insets(8, 0, 0, 0));
        getChildren().add(waitingLabel);

        GridPane grid = buildGrid();
        getChildren().add(grid);
    }

    private GridPane buildGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(6);
        grid.setPadding(new Insets(4, 0, 0, 0));
        grid.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints nameCol = new ColumnConstraints();
        nameCol.setPercentWidth(48);
        ColumnConstraints valCol = new ColumnConstraints();
        valCol.setPercentWidth(52);
        grid.getColumnConstraints().addAll(nameCol, valCol);

        int row = 0;
        for (Map.Entry<String, String> entry : FRIENDLY_NAMES.entrySet()) {
            String key = entry.getKey();

            Label nameLabel = new Label(entry.getValue());
            nameLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
            nameLabel.setTextFill(Color.web("#5a8090"));
            nameLabel.setPadding(new Insets(4, 8, 4, 8));
            nameLabel.setMaxWidth(Double.MAX_VALUE);
            nameLabel.setStyle(
                "-fx-background-color: #0d1e2e;" +
                "-fx-border-color: #1a2e42;" +
                "-fx-border-radius: 4 0 0 4; -fx-background-radius: 4 0 0 4;"
            );

            Label valueLabel = new Label("—");
            valueLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
            valueLabel.setTextFill(Color.web("#4a8fa8"));
            valueLabel.setPadding(new Insets(4, 10, 4, 10));
            valueLabel.setMaxWidth(Double.MAX_VALUE);
            valueLabel.setStyle(
                "-fx-background-color: #0d1e2e;" +
                "-fx-border-color: #1a2e42; -fx-border-width: 1 1 1 0;" +
                "-fx-border-radius: 0 4 4 0; -fx-background-radius: 0 4 4 0;"
            );

            grid.add(nameLabel,  0, row);
            grid.add(valueLabel, 1, row);
            valueLabels.put(key, valueLabel);
            row++;
        }
        return grid;
    }

    /** Called by LiveAlertPoller on the JavaFX thread with {"FLOODGAUGE":"12.50 m", ...}. */
    public void updateReadings(Map<String, String> readings) {
        boolean hasData = false;
        for (Map.Entry<String, String> entry : readings.entrySet()) {
            String key   = entry.getKey();
            String value = entry.getValue();
            Label lbl = valueLabels.get(key);
            if (lbl == null) continue;
            hasData = true;
            lbl.setText(value);
            lbl.setTextFill(severityColor(key, value));
        }
        if (hasData) waitingLabel.setVisible(false);
    }

    /** Colours the value label green/yellow/red based on thresholds. */
    private Color severityColor(String sensorType, String valueWithUnit) {
        double[] thresholds = WARN_CRIT.get(sensorType);
        if (thresholds == null) return Color.web("#4a8fa8");
        try {
            double v = Double.parseDouble(valueWithUnit.split(" ")[0]);
            double warn = thresholds[0], crit = thresholds[1];

            boolean inverted = sensorType.equals("BAROMETER"); // low pressure = danger
            if (inverted) {
                if (v <= crit) return Color.web("#e06c75");
                if (v <= warn) return Color.web("#e5a840");
            } else {
                if (v >= crit) return Color.web("#e06c75");
                if (v >= warn) return Color.web("#e5a840");
            }
        } catch (NumberFormatException ignored) {}
        return Color.web("#00cc66");
    }

    private Label sectionHeader(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        l.setTextFill(Color.web("#4a8fa8"));
        l.setPadding(new Insets(0, 0, 10, 0));
        return l;
    }
}
