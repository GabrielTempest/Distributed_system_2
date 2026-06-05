package com.eoc.dashboard.view;

import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Duration;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class HeaderBar extends HBox {

    private final Label clockLabel     = new Label();
    private final Label nodeCountLabel = new Label("8 nodes active");

    public HeaderBar() {
        setAlignment(Pos.CENTER_LEFT);
        setMinHeight(56);
        setPrefHeight(56);
        setPadding(new Insets(0, 24, 0, 24));
        setStyle("-fx-background-color: #07111e;");

        Label leftTitle = new Label("VIETNAM DISASTER EOC — SENSOR GRID");
        leftTitle.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        leftTitle.setTextFill(Color.web("#4a8fa8"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label sysLabel = new Label("EARLY WARNING SYSTEM");
        sysLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        sysLabel.setTextFill(Color.web("#00cc66"));

        Label sep1 = separator();
        Label sep2 = separator();

        nodeCountLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        nodeCountLabel.setTextFill(Color.web("#56b6c2"));

        clockLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        clockLabel.setTextFill(Color.web("#7ab8cc"));

        getChildren().addAll(leftTitle, spacer, sysLabel, sep1, nodeCountLabel, sep2, clockLabel);

        setBorder(new Border(new BorderStroke(
            Color.web("#1a2e42"), BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY, new BorderWidths(0, 0, 1, 0)
        )));

        startClock();
    }

    public void setNodeCount(int n) {
        nodeCountLabel.setText(n + " nodes active");
    }

    private void startClock() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss  dd-MMM-yyyy");
        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(1),
            e -> clockLabel.setText(LocalDateTime.now().format(fmt))));
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();
        clockLabel.setText(LocalDateTime.now().format(fmt));
    }

    private Label separator() {
        Label l = new Label("  |  ");
        l.setTextFill(Color.web("#1a2e42"));
        l.setFont(Font.font("Monospace", 13));
        return l;
    }
}
