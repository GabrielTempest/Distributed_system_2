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
        setPadding(new Insets(14, 16, 12, 16));
        setStyle("-fx-background-color: #070e18;");
        setBorder(new Border(new BorderStroke(
            Color.web("#0e1e2e"), BorderStrokeStyle.SOLID,
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

        Rectangle bar = new Rectangle(3, 44);
        bar.setFill(Color.web(q.getAccentColor()));
        bar.setArcWidth(3); bar.setArcHeight(3);
        HBox.setMargin(bar, new Insets(0, 12, 0, 0));

        VBox text = new VBox(4);
        HBox.setHgrow(text, Priority.ALWAYS);

        Label topic = new Label(q.getTopicPattern());
        topic.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        topic.setTextFill(Color.web(q.getAccentColor()));

        Label desc = new Label(q.getDescription() + (q.isCascade() ? "  ↑ cascade" : ""));
        desc.setFont(Font.font("Monospace", 10));
        desc.setTextFill(Color.web("#2a4050"));

        text.getChildren().addAll(topic, desc);
        row.getChildren().addAll(bar, text);
        return row;
    }

    private Label sectionHeader(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        l.setTextFill(Color.web("#1e3848"));
        l.setPadding(new Insets(0, 0, 6, 0));
        return l;
    }
}
