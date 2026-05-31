package com.eoc.dashboard.view;

import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.util.Duration;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class HeaderBar extends HBox {

    private final Label clockLabel    = new Label();
    private final Label nodeCountLabel = new Label("8 nodes active");

    public HeaderBar() {
        setAlignment(Pos.CENTER_LEFT);
        setMinHeight(48);
        setPrefHeight(48);
        setStyle("-fx-background-color: #060d18;");

        // Left title block
        Label leftTitle = new Label("VIETNAM DISASTER EOC — SENSOR GRID");
        leftTitle.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        leftTitle.setTextFill(Color.web("#2a5060"));
        leftTitle.setPadding(new Insets(0, 0, 0, 18));
        leftTitle.setMinWidth(380);
        leftTitle.setMaxWidth(380);

        // Vertical divider
        Rectangle div = new Rectangle(1, 48);
        div.setFill(Color.web("#0e1e2e"));

        // Right block
        VBox rightBox = new VBox(3);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.setPadding(new Insets(8, 18, 8, 18));
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        Label title = new Label("EARLY WARNING SYSTEM");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        title.setTextFill(Color.web("#00cc66"));

        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_RIGHT);

        Label tech = new Label("Kafka · PostgreSQL · Python");
        tech.setFont(Font.font("Monospace", 10));
        tech.setTextFill(Color.web("#1e3848"));

        Label sep1 = dot(); Label sep2 = dot();

        nodeCountLabel.setFont(Font.font("Monospace", 10));
        nodeCountLabel.setTextFill(Color.web("#1e3848"));

        clockLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        clockLabel.setTextFill(Color.web("#1e4858"));

        meta.getChildren().addAll(tech, sep1, nodeCountLabel, sep2, clockLabel);
        rightBox.getChildren().addAll(title, meta);

        getChildren().addAll(leftTitle, div, rightBox);

        // Bottom border
        setBorder(new Border(new BorderStroke(
            Color.web("#0e1e2e"), BorderStrokeStyle.SOLID,
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

    private Label dot() {
        Label l = new Label("·");
        l.setTextFill(Color.web("#0e1e2e"));
        l.setFont(Font.font("Monospace", 11));
        return l;
    }
}
