package com.eoc.dashboard.view;

import com.eoc.dashboard.model.Scenario;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;

import java.util.List;
import java.util.function.Consumer;

public class ScenarioPanel extends VBox {

    private Consumer<Scenario> onScenarioSelected;
    private HBox     activeCard     = null;
    private Scenario activeScenario = null;

    public ScenarioPanel() {
        setSpacing(6);
        setPadding(new Insets(16, 16, 12, 16));
        setStyle("-fx-background-color: #070e18;");
        setBorder(new Border(new BorderStroke(
            Color.web("#0e1e2e"), BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY, new BorderWidths(0, 0, 1, 0)
        )));
        getChildren().add(sectionHeader("RUN SCENARIO"));
    }

    public void setOnScenarioSelected(Consumer<Scenario> h) { onScenarioSelected = h; }

    public void loadScenarios(List<Scenario> scenarios) {
        getChildren().subList(1, getChildren().size()).clear();
        scenarios.forEach(s -> getChildren().add(buildCard(s)));
    }

    private HBox buildCard(Scenario scenario) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 14, 10, 14));
        card.setCursor(Cursor.HAND);
        card.setMaxWidth(Double.MAX_VALUE);
        styleCard(card, scenario.getColorHex(), false);

        Circle icon = new Circle(6);
        icon.setFill(Color.web(scenario.getColorHex()));

        VBox text = new VBox(3);
        HBox.setHgrow(text, Priority.ALWAYS);

        Label name = new Label(scenario.getName());
        name.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        name.setTextFill(Color.web(scenario.getColorHex()));

        Label desc = new Label(scenario.getDescription());
        desc.setFont(Font.font("Monospace", 10));
        desc.setTextFill(Color.web("#2e4a5a"));

        text.getChildren().addAll(name, desc);

        Label badge = new Label(scenario.getSeverityLabel());
        badge.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        badge.setTextFill(Color.web(scenario.getColorHex()));
        badge.setPadding(new Insets(2, 6, 2, 6));
        badge.setStyle(
            "-fx-background-color: " + scenario.getColorHex() + "22;" +
            "-fx-border-color: "     + scenario.getColorHex() + "55;" +
            "-fx-border-radius: 3; -fx-background-radius: 3;"
        );

        card.getChildren().addAll(icon, text, badge);

        card.setOnMouseEntered(e -> { if (card != activeCard) styleCard(card, scenario.getColorHex(), true); });
        card.setOnMouseExited (e -> { if (card != activeCard) styleCard(card, scenario.getColorHex(), false); });
        card.setOnMouseClicked(e -> {
            if (activeCard != null && activeScenario != null)
                styleCard(activeCard, activeScenario.getColorHex(), false);
            activeCard = card; activeScenario = scenario;
            styleCard(card, scenario.getColorHex(), true);
            if (onScenarioSelected != null) onScenarioSelected.accept(scenario);
        });

        return card;
    }

    private void styleCard(HBox card, String hex, boolean active) {
        card.setStyle(
            "-fx-background-color: " + (active ? hex + "1a" : "#0c1620") + ";" +
            "-fx-border-color: "     + (active ? hex + "66" : "#142030") + ";" +
            "-fx-border-radius: 4; -fx-background-radius: 4; -fx-border-width: 1;"
        );
    }

    private Label sectionHeader(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        l.setTextFill(Color.web("#1e3848"));
        l.setPadding(new Insets(0, 0, 6, 0));
        return l;
    }
}
