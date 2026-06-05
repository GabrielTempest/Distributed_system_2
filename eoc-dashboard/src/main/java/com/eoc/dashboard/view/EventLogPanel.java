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
        setPadding(new Insets(20, 20, 16, 20));
        setStyle("-fx-background-color: #060e18;");
        VBox.setVgrow(this, Priority.ALWAYS);

        Label header = sectionHeader("EVENT LOG");

        listView.setStyle(
            "-fx-background-color: #0a1628;" +
            "-fx-border-color: #1a2e42;" +
            "-fx-border-radius: 6; -fx-background-radius: 6;"
        );
        listView.setPrefHeight(300);
        listView.setMinHeight(150);
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
                setFont(Font.font("Monospace", FontWeight.NORMAL, 12));
                setStyle("-fx-background-color: transparent; -fx-padding: 4 10 4 10;");

                if      (item.contains(" ERROR ")) setTextFill(Color.web("#e06c75"));
                else if (item.contains(" WARN  ")) setTextFill(Color.web("#e5a840"));
                else if (item.contains(" INFO  ")) setTextFill(Color.web("#56b6c2"));
                else if (item.contains(" LIVE  ")) setTextFill(Color.web("#c678dd"));
                else                               setTextFill(Color.web("#7ab8cc"));
            }
        });

        Label placeholder = new Label("Run a scenario or start the backend to see events…");
        placeholder.setTextFill(Color.web("#2a4050"));
        placeholder.setFont(Font.font("Monospace", 12));
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
        l.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        l.setTextFill(Color.web("#4a8fa8"));
        l.setPadding(new Insets(0, 0, 10, 0));
        return l;
    }
}
