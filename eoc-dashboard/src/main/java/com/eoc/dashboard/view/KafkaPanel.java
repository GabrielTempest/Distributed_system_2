package com.eoc.dashboard.view;

import com.eoc.dashboard.model.KafkaQueue;
import javafx.geometry.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;

import java.util.List;

public class KafkaPanel extends VBox {

    public KafkaPanel() {
        setSpacing(0);
        setPadding(new Insets(20, 20, 16, 20));
        setStyle("-fx-background-color: #0a1628;");
        setBorder(new Border(new BorderStroke(
            Color.web("#1a2e42"), BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY, new BorderWidths(0, 0, 1, 0)
        )));
        getChildren().add(sectionHeader("KAFKA QUEUE HIERARCHY"));
    }

    public void loadQueues(List<KafkaQueue> queues) {
        getChildren().subList(1, getChildren().size()).clear();
        queues.forEach(q -> getChildren().add(buildRow(q)));
    }

    private HBox buildRow(KafkaQueue q) {
        HBox row = new HBox();
        row.setPadding(new Insets(10, 0, 10, 0));
        row.setAlignment(Pos.TOP_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setStyle(
            "-fx-background-color: #0d1e2e;" +
            "-fx-border-color: #1a2e42;" +
            "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;"
        );
        row.setPadding(new Insets(12, 14, 12, 14));

        Rectangle bar = new Rectangle(3, 40);
        bar.setFill(Color.web(q.getAccentColor()));
        bar.setArcWidth(3); bar.setArcHeight(3);
        HBox.setMargin(bar, new Insets(2, 14, 0, 0));

        VBox text = new VBox(5);
        HBox.setHgrow(text, Priority.ALWAYS);

        Label topic = new Label(q.getTopicPattern());
        topic.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        topic.setTextFill(Color.web(q.getAccentColor()));
        topic.setWrapText(true);

        Label desc = new Label(q.getDescription() + (q.isCascade() ? "  ↑ cascade" : ""));
        desc.setFont(Font.font("Monospace", 11));
        desc.setTextFill(Color.web("#5a8090"));
        desc.setWrapText(true);

        text.getChildren().addAll(topic, desc);
        row.getChildren().addAll(bar, text);

        VBox.setMargin(row, new Insets(0, 0, 6, 0));
        return row;
    }

    private Label sectionHeader(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        l.setTextFill(Color.web("#4a8fa8"));
        l.setPadding(new Insets(0, 0, 10, 0));
        return l;
    }
}
