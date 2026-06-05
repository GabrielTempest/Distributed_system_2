package com.eoc.dashboard.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.util.List;

/**
 * TableView showing every field of Alert consumed by national_eoc's UIConsumer.
 * Polled from GET http://localhost:8090/api/ui/alerts every 3 seconds.
 */
public class AlertTablePanel extends VBox {

    public record AlertRow(
        String alertId,
        String localEocId,
        String disasterType,
        String alertType,
        String message,
        String timestamp
    ) {}

    private final ObservableList<AlertRow> rows = FXCollections.observableArrayList();
    private final TableView<AlertRow> table = new TableView<>(rows);
    private final Label countLabel = new Label("0 alerts");

    public AlertTablePanel() {
        setSpacing(0);
        setPadding(new Insets(20, 20, 20, 20));
        setStyle("-fx-background-color: #060e18;");
        VBox.setVgrow(this, Priority.ALWAYS);

        // Header row
        HBox headerRow = new HBox();
        headerRow.setPadding(new Insets(0, 0, 10, 0));

        Label title = new Label("KAFKA ALERT FEED  —  national_eoc UIConsumer");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        title.setTextFill(Color.web("#4a8fa8"));
        HBox.setHgrow(title, Priority.ALWAYS);

        countLabel.setFont(Font.font("Monospace", 11));
        countLabel.setTextFill(Color.web("#5a8090"));

        headerRow.getChildren().addAll(title, countLabel);

        buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        getChildren().addAll(headerRow, table);
    }

    private void buildTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setStyle(
            "-fx-background-color: #0a1628;" +
            "-fx-border-color: #1a2e42;" +
            "-fx-border-radius: 6; -fx-background-radius: 6;" +
            "-fx-table-cell-border-color: #1a2e42;"
        );
        table.setPlaceholder(new Label("Waiting for alerts from national_eoc…") {{
            setTextFill(Color.web("#2a4050"));
            setFont(Font.font("Monospace", 12));
        }});

        table.getColumns().addAll(
            col("Time",          120, r -> shortTime(r.timestamp())),
            col("EOC",            90, r -> r.localEocId()),
            col("Disaster",       90, r -> r.disasterType()),
            col("Level",          70, r -> r.alertType()),
            col("Alert ID",      130, r -> r.alertId().length() > 12
                                            ? r.alertId().substring(0, 12) + "…"
                                            : r.alertId()),
            col("Message",       300, r -> r.message())
        );

        // Colour rows by alert level
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(AlertRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                String bg = switch (item.alertType()) {
                    case "RED"    -> "#2a0d10";
                    case "ORANGE" -> "#2a1a08";
                    case "YELLOW" -> "#1e1e08";
                    default       -> "#0d1e2e";
                };
                setStyle("-fx-background-color: " + bg + ";");
            }
        });

        styleHeaderCells();
    }

    private TableColumn<AlertRow, String> col(String title, double minWidth,
                                               java.util.function.Function<AlertRow, String> getter) {
        TableColumn<AlertRow, String> c = new TableColumn<>(title);
        c.setMinWidth(minWidth);
        c.setCellValueFactory(p -> new SimpleStringProperty(getter.apply(p.getValue())));
        c.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                setFont(Font.font("Monospace", 11));
                // Level column: colour by severity
                if (tc.getText() != null && title.equals("Level")) {
                    setTextFill(levelColor(item));
                    setFont(Font.font("Monospace", FontWeight.BOLD, 11));
                } else {
                    setTextFill(Color.web("#7ab8cc"));
                }
                setStyle("-fx-background-color: transparent; -fx-padding: 4 8 4 8;");
            }
        });
        return c;
    }

    private void styleHeaderCells() {
        table.skinProperty().addListener((obs, o, n) -> {
            if (n == null) return;
            table.lookupAll(".column-header").forEach(node ->
                node.setStyle(
                    "-fx-background-color: #0d1e2e;" +
                    "-fx-border-color: #1a2e42;" +
                    "-fx-font-family: monospace; -fx-font-size: 11px;"
                )
            );
            table.lookupAll(".column-header-background").forEach(node ->
                node.setStyle("-fx-background-color: #0d1e2e;")
            );
        });
    }

    public void setAlerts(List<AlertRow> newRows) {
        rows.setAll(newRows);
        countLabel.setText(newRows.size() + " alert" + (newRows.size() == 1 ? "" : "s"));
        if (!rows.isEmpty()) table.scrollTo(rows.size() - 1);
    }

    private Color levelColor(String level) {
        return switch (level) {
            case "RED"    -> Color.web("#e06c75");
            case "ORANGE" -> Color.web("#e5a840");
            case "YELLOW" -> Color.web("#d4c44a");
            default       -> Color.web("#00cc66");
        };
    }

    private String shortTime(String iso) {
        if (iso == null || iso.length() < 19) return iso;
        return iso.substring(11, 19); // HH:mm:ss from ISO-8601
    }
}
