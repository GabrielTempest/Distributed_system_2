package com.eoc.dashboard.view;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

public class EventLogPanel extends VBox {

    private final ListView<String> listView = new ListView<>();

    public EventLogPanel() {
        setSpacing(0);
        setPadding(new Insets(14, 16, 12, 16));
        setStyle("-fx-background-color: #070e18;");
        setBorder(new Border(new BorderStroke(
            Color.web("#0e1e2e"), BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY, new BorderWidths(0, 0, 1, 0)
        )));
        VBox.setVgrow(this, Priority.ALWAYS);

        Label header = sectionHeader("EVENT LOG");

        listView.setStyle(
            "-fx-background-color: #050c14;" +
            "-fx-border-color: #0e1e2e;" +
            "-fx-border-radius: 3; -fx-background-radius: 3;"
        );
        listView.setPrefHeight(200);
        listView.setMinHeight(100);
        VBox.setVgrow(listView, Priority.ALWAYS);

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                setText(item);
                setFont(Font.font("Monospace", FontWeight.NORMAL, 10));
                setStyle("-fx-background-color: transparent; -fx-padding: 2 6 2 6;");
                if      (item.contains(" ERROR ")) setTextFill(Color.web("#e06c75"));
                else if (item.contains(" WARN  ")) setTextFill(Color.web("#e5a840"));
                else if (item.contains(" INFO  ")) setTextFill(Color.web("#56b6c2"));
                else                               setTextFill(Color.web("#2a4050"));
            }
        });

        Label placeholder = new Label("Run a scenario to see events…");
        placeholder.setTextFill(Color.web("#1e3040"));
        placeholder.setFont(Font.font("Monospace", 10));
        listView.setPlaceholder(placeholder);

        getChildren().addAll(header, listView);
    }

    public void bindLog(ObservableList<String> log) {
        listView.setItems(log);
        log.addListener((javafx.collections.ListChangeListener<String>) c -> {
            if (!log.isEmpty()) listView.scrollTo(0);
        });
    }

    private Label sectionHeader(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        l.setTextFill(Color.web("#1e3848"));
        l.setPadding(new Insets(0, 0, 6, 0));
        return l;
    }
}
